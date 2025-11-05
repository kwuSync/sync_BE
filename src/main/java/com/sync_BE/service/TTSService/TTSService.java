package com.sync_BE.service.TTSService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	public TTSService(NewsService newsService, UserSettingRepository userSettingRepository) {
		this.newsService = newsService;
		this.userSettingRepository = userSettingRepository;
	}

	private Optional<UserSetting> getUserSetting(CustomUserDetails userDetails) {
		if (userDetails == null || userDetails.getUser() == null) {
			return Optional.empty();
		}
		return userSettingRepository.findByUserId(userDetails.getUser().getId());
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

		Optional<UserSetting> settingOpt = getUserSetting(userDetails);

		if (settingOpt.isPresent() && !settingOpt.get().isTtsEnabled()) {
			throw new IOException("사용자가 TTS 기능을 비활성화했습니다.");
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		for (String text : summaryTexts) {
			byte[] audioChunk = synthesize(text, settingOpt, ttsRequestDTO);
			outputStream.write(audioChunk);
		}

		return outputStream.toByteArray();
	}

	public byte[] synthesizeNewsSummary(String clusterId, CustomUserDetails userDetails, TTSRequestDTO ttsRequestDTO) throws IOException {
		NewsResponseDTO.NewsClusterDTO newsCluster = newsService.getNewsSummaryByClusterId(clusterId);
		if (newsCluster == null || newsCluster.getSummary() == null || newsCluster.getSummary().getArticle() == null) { //
			throw new IOException("요약된 뉴스를 찾을 수 없습니다.");
		}
		String summaryText = newsCluster.getSummary().getArticle();

		if (summaryText.isEmpty()) {
			throw new IOException("요약 텍스트가 비어있습니다.");
		}

		Optional<UserSetting> settingOpt = getUserSetting(userDetails);
		if (settingOpt.isPresent() && !settingOpt.get().isTtsEnabled()) {
			if (ttsRequestDTO == null) {
				throw new IOException("사용자가 TTS 기능을 비활성화했습니다.");
			}
		}
		return synthesize(summaryText, settingOpt, ttsRequestDTO);
	}

	private byte[] synthesize(String text, Optional<UserSetting> settingOpt, TTSRequestDTO ttsRequestDTO) throws IOException {
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
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
				// 사용자가 "Alnilam" 또는 "Achernar"를 직접 보낸 경우
				voiceBuilder.setName(effectiveVoiceName);
				voiceBuilder.setModelName(modelName);
			} else {
				// "ko-KR-Wavenet-A" 등 다른 음성을 지정한 경우 (이 경우 modelName 불필요)
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

			SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
			ByteString audioContents = response.getAudioContent();
			return audioContents.toByteArray();
		}
	}
}
