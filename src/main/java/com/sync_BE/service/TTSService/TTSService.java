package com.sync_BE.service.TTSService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.cloud.texttospeech.v1beta1.AudioConfig;
import com.google.cloud.texttospeech.v1beta1.AudioEncoding;
import com.google.cloud.texttospeech.v1beta1.SynthesisInput;
import com.google.cloud.texttospeech.v1beta1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1beta1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1beta1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import com.sync_BE.domain.UserSetting;
import com.sync_BE.repository.UserSettingRepository;
import com.sync_BE.security.CustomUserDetails;
import com.sync_BE.service.NewsService.NewsService;
import com.sync_BE.web.dto.TTSRequestDTO;
import com.sync_BE.web.dto.newsDTO.NewsResponseDTO;

@Service
public class TTSService {

	private final NewsService newsService;
	private final UserSettingRepository userSettingRepository;
	private final TextToSpeechClient textToSpeechClient;
	private final Executor ttsExecutor;

	public TTSService(NewsService newsService,
					  UserSettingRepository userSettingRepository,
					  TextToSpeechClient textToSpeechClient,
					  @Qualifier("ttsExecutor") Executor ttsExecutor) {
		this.newsService = newsService;
		this.userSettingRepository = userSettingRepository;
		this.textToSpeechClient = textToSpeechClient;
		this.ttsExecutor = ttsExecutor;
	}

	private Optional<UserSetting> getUserSetting(CustomUserDetails userDetails) {
		if (userDetails == null || userDetails.getUser() == null) {
			return Optional.empty();
		}
		return userSettingRepository.findByUserId(userDetails.getUser().getId());
	}

	private List<String> splitTextIntoChunks(String text) {
		if (text == null || text.trim().isEmpty()) {
			return List.of();
		}

		return Arrays.stream(text.split("\n"))
				.flatMap(paragraph ->
						Arrays.stream(paragraph.split("(?<=[.!?])\\s+"))
				)
				.map(String::trim)
				.filter(chunk -> !chunk.isEmpty())
				.collect(Collectors.toList());
	}

	public byte[] synthesizeMainSummary(CustomUserDetails userDetails, TTSRequestDTO ttsRequestDTO) throws IOException {
		NewsResponseDTO.NewsListDTO mainNewsList = newsService.getMain();

		if (mainNewsList == null || mainNewsList.getNewsList() == null || mainNewsList.getNewsList().isEmpty()) {
			throw new IOException("메인 요약 뉴스를 찾을 수 없습니다.");
		}

		List<String> summaryTexts = mainNewsList.getNewsList().stream()
				.map(NewsResponseDTO.NewsArticleDTO::getSummaryText)
				.filter(text -> text != null && !text.isEmpty())
				.collect(Collectors.toList());

		if (summaryTexts.isEmpty()) {
			throw new IOException("요약 텍스트가 비어있습니다.");
		}

		String allText = String.join("\n", summaryTexts);
		List<String> textChunks = splitTextIntoChunks(allText);

		if (textChunks.isEmpty()) {
			throw new IOException("변환할 유효한 텍스트 조각이 없습니다.");
		}

		Optional<UserSetting> settingOpt = getUserSetting(userDetails);

		if (settingOpt.isPresent() && !settingOpt.get().isTtsEnabled()) {
			throw new IOException("사용자가 TTS 기능을 비활성화했습니다.");
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			List<CompletableFuture<byte[]>> futures = summaryTexts.stream()
					.map(text -> CompletableFuture.supplyAsync(() -> {
						try {
							return synthesize(text, settingOpt, ttsRequestDTO);
						} catch (IOException e) {
							System.err.println("TTS 조각 변환 실패: " + e.getMessage() + " | TEXT: " + text.substring(0, Math.min(text.length(), 20)) + "...");
							throw new RuntimeException("TTS synthesis failed for a chunk", e);
						}
					}, ttsExecutor))
					.collect(Collectors.toList());

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

			for (CompletableFuture<byte[]> future : futures) {
				outputStream.write(future.join());
			}

		} catch (RuntimeException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw new IOException("Failed during parallel TTS synthesis", e);
			}
		}

		return outputStream.toByteArray();
	}

	public byte[] synthesizeNewsSummary(String clusterId, CustomUserDetails userDetails, TTSRequestDTO ttsRequestDTO) throws IOException {
		NewsResponseDTO.NewsClusterDTO newsCluster = newsService.getNewsSummaryByClusterId(clusterId);
		if (newsCluster == null || newsCluster.getSummary() == null || newsCluster.getSummary().getArticle() == null) {
			throw new IOException("요약된 뉴스를 찾을 수 없습니다.");
		}
		String summaryText = newsCluster.getSummary().getArticle();

		if (summaryText.isEmpty()) {
			throw new IOException("요약 텍스트가 비어있습니다.");
		}

		List<String> textChunks = splitTextIntoChunks(summaryText);

		if (textChunks.isEmpty()) {
			throw new IOException("변환할 유효한 텍스트 조각이 없습니다.");
		}

		Optional<UserSetting> settingOpt = getUserSetting(userDetails);
		if (settingOpt.isPresent() && !settingOpt.get().isTtsEnabled()) {
			if (ttsRequestDTO == null) {
				throw new IOException("사용자가 TTS 기능을 비활성화했습니다.");
			}
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			List<CompletableFuture<byte[]>> futures = textChunks.stream()
					.map(text -> CompletableFuture.supplyAsync(() -> {
						try {
							return synthesize(text, settingOpt, ttsRequestDTO);
						} catch (IOException e) {
							System.err.println("TTS 조각 변환 실패: " + e.getMessage() + " | TEXT: " + text.substring(0, Math.min(text.length(), 20)) + "...");
							throw new RuntimeException("TTS synthesis failed for a chunk", e);
						}
					}, ttsExecutor))
					.collect(Collectors.toList());

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

			for (CompletableFuture<byte[]> future : futures) {
				outputStream.write(future.join());
			}

		} catch (RuntimeException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw new IOException("Failed during parallel TTS synthesis", e);
			}
		}

		return outputStream.toByteArray();
	}

	private byte[] synthesize(String text, Optional<UserSetting> settingOpt, TTSRequestDTO ttsRequestDTO) throws IOException {
		SynthesisInput input = SynthesisInput.newBuilder()
				.setText(text)
				.build();

		VoiceSelectionParams.Builder voiceBuilder = VoiceSelectionParams.newBuilder();
		voiceBuilder.setLanguageCode("ko-KR");

		String effectiveVoiceName = null;

		if (ttsRequestDTO != null && ttsRequestDTO.getVoiceName() != null && !ttsRequestDTO.getVoiceName().isEmpty()) {
			effectiveVoiceName = ttsRequestDTO.getVoiceName();
		}
		else if (settingOpt.isPresent() && settingOpt.get().getTtsVoiceName() != null) {
			effectiveVoiceName = settingOpt.get().getTtsVoiceName();
		}
		else {
			effectiveVoiceName = "FEMALE";
		}

		String modelName = "gemini-2.5-pro-tts";

		if ("MALE".equalsIgnoreCase(effectiveVoiceName)) {
			voiceBuilder.setName("Alnilam");
			voiceBuilder.setModelName(modelName);
		} else if ("FEMALE".equalsIgnoreCase(effectiveVoiceName)) {
			voiceBuilder.setName("Achernar");
			voiceBuilder.setModelName(modelName);
		} else if (effectiveVoiceName.equals("Alnilam") || effectiveVoiceName.equals("Achernar")) {
			voiceBuilder.setName(effectiveVoiceName);
			voiceBuilder.setModelName(modelName);
		} else {
			voiceBuilder.setName(effectiveVoiceName);
		}

		VoiceSelectionParams voice = voiceBuilder.build();

		AudioConfig.Builder audioBuilder = AudioConfig.newBuilder()
				.setAudioEncoding(AudioEncoding.MP3);

		Double pitch = null;
		if (ttsRequestDTO != null && ttsRequestDTO.getPitch() != null) {
			pitch = ttsRequestDTO.getPitch();
		} else if (settingOpt.isPresent()) {
			pitch = settingOpt.get().getPitch();
		}
		if (pitch != null) {
			audioBuilder.setPitch(pitch);
		}

		Double speakingRate = null;
		if (ttsRequestDTO != null && ttsRequestDTO.getSpeakingRate() != null) {
			speakingRate = ttsRequestDTO.getSpeakingRate();
		} else if (settingOpt.isPresent()) {
			speakingRate = settingOpt.get().getSpeakingRate();
		}

		if (speakingRate != null) {
			audioBuilder.setSpeakingRate(speakingRate);
		}

		AudioConfig audioConfig = audioBuilder.build();

		SynthesizeSpeechResponse response = this.textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
		ByteString audioContents = response.getAudioContent();
		return audioContents.toByteArray();
	}
}

