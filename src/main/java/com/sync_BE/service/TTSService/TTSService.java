package com.sync_BE.service.TTSService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TTSService {

	private static final int MAIN_MAX_CONCURRENCY = 8;
	private static final int DEFAULT_MAX_CONCURRENCY = 5;

	private static final int MAX_CHUNK_LEN = 4500;

	private static final String DEFAULT_MODEL = "gemini-2.5-pro-tts";

	private final NewsService newsService;
	private final UserSettingRepository userSettingRepository;

	private final ExecutorService ttsExecutor = Executors.newFixedThreadPool(
			Math.max(MAIN_MAX_CONCURRENCY, DEFAULT_MAX_CONCURRENCY)
	);

	private final Semaphore mainSemaphore = new Semaphore(MAIN_MAX_CONCURRENCY);
	private final Semaphore defaultSemaphore = new Semaphore(DEFAULT_MAX_CONCURRENCY);

	public TTSService(NewsService newsService,
					  UserSettingRepository userSettingRepository) {
		this.newsService = newsService;
		this.userSettingRepository = userSettingRepository;
	}

	public byte[] synthesizeMainSummary(CustomUserDetails userDetails, TTSRequestDTO req) throws IOException {
		NewsResponseDTO.NewsListDTO mainNewsList = newsService.getMain();
		if (mainNewsList == null || mainNewsList.getNewsList() == null || mainNewsList.getNewsList().isEmpty()) {
			throw new IOException("메인 요약 뉴스를 찾을 수 없습니다.");
		}

		List<String> summaries = mainNewsList.getNewsList().stream()
				.map(NewsResponseDTO.NewsArticleDTO::getSummaryText)
				.filter(s -> s != null && !s.isBlank())
				.collect(Collectors.toList());
		if (summaries.isEmpty()) throw new IOException("요약 텍스트가 비어있습니다.");

		Optional<UserSetting> settingOpt = getUserSetting(userDetails);
		guardTtsEnabled(settingOpt, req);

		List<CompletableFuture<byte[]>> futures = new ArrayList<>();
		for (int i = 0; i < summaries.size(); i++) {
			final int idx = i;
			final String piece = normalizeBullets(summaries.get(i));

			futures.add(CompletableFuture.supplyAsync(() -> {
				try {
					mainSemaphore.acquire();
					return callWithRetry(() -> {
						try {
							return synthesizeOneRequest(piece, settingOpt, req);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}, idx, true);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("mainTTS 인터럽트", ie);
				} finally {
					mainSemaphore.release();
				}
			}, ttsExecutor));
		}

		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			for (int i = 0; i < futures.size(); i++) {
				try {
					byte[] data = futures.get(i).join();
					if (data != null) os.write(data);
				} catch (RuntimeException re) {
					Throwable cause = re.getCause() != null ? re.getCause() : re;
					log.error("mainTTS 병렬 실패: idx={}, msg={}", i, cause.getMessage(), cause);
				}
			}
			return os.toByteArray();
		}
	}

	public byte[] synthesizeNewsSummary(String clusterId, CustomUserDetails userDetails, TTSRequestDTO req) throws IOException {
		NewsResponseDTO.NewsClusterDTO cluster = newsService.getNewsSummaryByClusterId(clusterId);
		if (cluster == null || cluster.getSummary() == null || cluster.getSummary().getArticle() == null) {
			throw new IOException("요약된 뉴스를 찾을 수 없습니다.");
		}
		String article = cluster.getSummary().getArticle();
		Optional<UserSetting> settingOpt = getUserSetting(userDetails);
		guardTtsEnabled(settingOpt, req);
		return synthesizeLongText(article, settingOpt, req);
	}

	private byte[] synthesizeLongText(String text, Optional<UserSetting> settingOpt, TTSRequestDTO req) throws IOException {
		List<String> chunks = splitTextIntoChunks(text);
		if (chunks.isEmpty()) throw new IOException("변환할 유효한 텍스트 조각이 없습니다.");

		List<CompletableFuture<byte[]>> futures = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i++) {
			final int idx = i;
			final String part = chunks.get(i);
			futures.add(CompletableFuture.supplyAsync(() -> {
				try {
					defaultSemaphore.acquire();
					return callWithRetry(() -> {
						try {
							return synthesizeOneRequest(part, settingOpt, req);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}, idx, false);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("articleTTS 인터럽트", ie);
				} finally {
					defaultSemaphore.release();
				}
			}, ttsExecutor));
		}

		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			for (int i = 0; i < futures.size(); i++) {
				try {
					byte[] data = futures.get(i).join();
					if (data != null) os.write(data);
				} catch (RuntimeException re) {
					Throwable cause = re.getCause() != null ? re.getCause() : re;
					log.error("articleTTS 병렬 실패: idx={}, msg={}", i, cause.getMessage(), cause);
					throw new IOException("Failed during article TTS synthesis: idx=" + i + ", msg=" + cause.getMessage(), cause);
				}
			}
			return os.toByteArray();
		}
	}

	private byte[] synthesizeOneRequest(String text, Optional<UserSetting> settingOpt, TTSRequestDTO req) throws IOException {
		validateNoControlChars(text);

		SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
		VoiceSelectionParams voice = buildVoiceParams(settingOpt, req);
		AudioConfig audio = buildAudioConfig(settingOpt, req);

		try (TextToSpeechClient client = TextToSpeechClient.create()) {
			SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audio);
			ByteString audioContents = response.getAudioContent();
			return audioContents.toByteArray();
		} catch (Exception e) {
			throw new IOException("TTS 변환 실패: " + e.getMessage(), e);
		}
	}

	private Optional<UserSetting> getUserSetting(CustomUserDetails userDetails) {
		if (userDetails == null || userDetails.getUser() == null) return Optional.empty();
		return userSettingRepository.findByUserId(userDetails.getUser().getId());
	}

	private void guardTtsEnabled(Optional<UserSetting> settingOpt, TTSRequestDTO req) throws IOException {
		if (settingOpt.isPresent() && !settingOpt.get().isTtsEnabled()) {
			if (req == null) throw new IOException("사용자가 TTS 기능을 비활성화했습니다.");
		}
	}

	private String normalizeBullets(String s) {
		String normalized = Arrays.stream(s.split("\n"))
				.map(String::trim)
				.filter(line -> !line.isEmpty())
				.map(line -> line.startsWith("-") && line.length() > 1 ? line.substring(1).trim() : line)
				.collect(Collectors.joining(". "));
		if (!normalized.endsWith(".")) normalized += ".";
		return normalized;
	}

	private List<String> splitTextIntoChunks(String text) {
		if (text == null || text.isBlank()) return List.of();

		List<String> sentences = Arrays.stream(text.split("\n"))
				.flatMap(p -> Arrays.stream(p.split("(?<=[.!?])\\s+")))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());

		List<String> chunks = new ArrayList<>();
		for (String s : sentences) {
			if (s.length() <= MAX_CHUNK_LEN) {
				chunks.add(s);
			} else {
				chunks.addAll(splitByLength(s, MAX_CHUNK_LEN));
			}
		}
		return chunks;
	}

	private List<String> splitByLength(String text, int limit) {
		List<String> res = new ArrayList<>();
		for (int i = 0; i < text.length(); i += limit) {
			res.add(text.substring(i, Math.min(i + limit, text.length())));
		}
		return res;
	}

	private void validateNoControlChars(String text) throws IOException {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isISOControl(c) && !Character.isWhitespace(c)) {
				throw new IOException("제어문자 포함: U+" + String.format("%04X", (int) c));
			}
		}
	}

	private byte[] callWithRetry(Supplier<byte[]> call, int idx, boolean isMain) {
		int maxRetry = 2;
		long backoff = 200L;

		for (int attempt = 0; attempt <= maxRetry; attempt++) {
			try {
				return call.get();
			} catch (RuntimeException e) {
				Throwable cause = e.getCause() != null ? e.getCause() : e;
				if (attempt == maxRetry) {
					throw new RuntimeException("TTS 실패 (최대 재시도 초과): " + cause.getMessage(), cause);
				}
				long wait = backoff * (1L << attempt);
				log.warn("TTS 재시도 예정: kind={}, idx={}, attempt={}, wait={}ms, msg={}",
						isMain ? "main" : "article", idx, attempt + 1, wait, cause.getMessage());
				try {
					Thread.sleep(wait);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("TTS 재시도 인터럽트", ie);
				}
			}
		}
		throw new RuntimeException("unreachable");
	}

	private VoiceSelectionParams buildVoiceParams(Optional<UserSetting> settingOpt, TTSRequestDTO req) {
		String v = null;
		if (req != null && req.getVoiceName() != null && !req.getVoiceName().isBlank()) {
			v = req.getVoiceName();
		} else if (settingOpt.isPresent() && settingOpt.get().getTtsVoiceName() != null && !settingOpt.get().getTtsVoiceName().isBlank()) {
			v = settingOpt.get().getTtsVoiceName();
		}
		if (v == null) v = "FEMALE";

		List<String> allowed = List.of("FEMALE", "MALE", "Achernar", "Alnilam");
		if (!allowed.contains(v)) {
			log.warn("지원하지 않는 voiceName='{}' → 기본값(FEMALE)로 대체", v);
			v = "FEMALE";
		}

		VoiceSelectionParams.Builder vb = VoiceSelectionParams.newBuilder().setLanguageCode("ko-KR");
		if ("MALE".equalsIgnoreCase(v)) {
			vb.setName("Alnilam").setModelName(DEFAULT_MODEL);
		} else if ("FEMALE".equalsIgnoreCase(v)) {
			vb.setName("Achernar").setModelName(DEFAULT_MODEL);
		} else if ("Alnilam".equals(v) || "Achernar".equals(v)) {
			vb.setName(v).setModelName(DEFAULT_MODEL);
		} else {
			vb.setName(v);
		}
		return vb.build();
	}

	private AudioConfig buildAudioConfig(Optional<UserSetting> settingOpt, TTSRequestDTO req) {
		Double pitch = null;
		Double rate = null;

		if (req != null) {
			pitch = req.getPitch();
			rate = req.getSpeakingRate();
		}
		if (pitch == null && settingOpt.isPresent()) pitch = settingOpt.get().getPitch();
		if (rate == null && settingOpt.isPresent()) rate = settingOpt.get().getSpeakingRate();

		if (pitch != null && (pitch < -20.0 || pitch > 20.0)) {
			log.warn("pitch 범위 초과: {} → 0.0으로 교정", pitch);
			pitch = 0.0;
		}
		if (rate != null && (rate < 0.25 || rate > 4.0)) {
			log.warn("speakingRate 범위 초과: {} → 1.0으로 교정", rate);
			rate = 1.0;
		}

		AudioConfig.Builder ab = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3);
		if (pitch != null) ab.setPitch(pitch);
		if (rate != null) ab.setSpeakingRate(rate);
		return ab.build();
	}
}
