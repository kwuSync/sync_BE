package com.sync_BE.service.TTSService;

import com.google.cloud.texttospeech.v1beta1.*;
import com.google.protobuf.ByteString;
import com.sync_BE.domain.UserSetting;
import com.sync_BE.repository.UserSettingRepository;
import com.sync_BE.security.CustomUserDetails;
import com.sync_BE.service.NewsService.NewsService;
import com.sync_BE.web.dto.TTSRequestDTO;
import com.sync_BE.web.dto.newsDTO.NewsResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TTSService {

	private static final String MODEL = "chirp-3";
	private static final int MAX_CONCURRENCY = 2;
	private static final int MAX_CHUNK_LEN = 2500;

	private final ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENCY);
	private final NewsService newsService;
	private final UserSettingRepository userSettingRepository;

	public TTSService(NewsService newsService, UserSettingRepository userSettingRepository) {
		this.newsService = newsService;
		this.userSettingRepository = userSettingRepository;
	}

	public byte[] synthesizeMainSummary(CustomUserDetails user, TTSRequestDTO dto, int page, int pageSize) throws IOException {
		var mainNews = newsService.getMain();
		if (mainNews == null || mainNews.getNewsList() == null || mainNews.getNewsList().isEmpty()) {
			throw new IOException("요약 뉴스가 없습니다.");
		}

		List<String> texts = mainNews.getNewsList().stream()
				.map(NewsResponseDTO.NewsArticleDTO::getSummaryText)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		int from = Math.max(0, (page - 1) * pageSize);
		int to = Math.min(from + pageSize, texts.size());
		if (from >= texts.size()) throw new IOException("요청한 페이지의 기사가 없습니다.");

		List<String> pageTexts = texts.subList(from, to);
		log.info("🎧 TTS 요청: page={} (기사 {}~{})", page, from + 1, to);

		return synthesizeTexts(pageTexts, user, dto);
	}

	public byte[] synthesizeNewsSummary(String clusterId, CustomUserDetails user, TTSRequestDTO dto) throws IOException {
		var cluster = newsService.getNewsSummaryByClusterId(clusterId);
		if (cluster == null || cluster.getSummary() == null) throw new IOException("클러스터 요약을 찾을 수 없습니다.");
		return synthesizeTexts(List.of(cluster.getSummary().getArticle()), user, dto);
	}

	private byte[] synthesizeTexts(List<String> texts, CustomUserDetails user, TTSRequestDTO dto) throws IOException {
		Optional<UserSetting> settingOpt = getUserSetting(user);
		List<String> chunks = splitIntoChunks(
				texts.stream().map(this::cleanText).collect(Collectors.toList())
		);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Semaphore limiter = new Semaphore(MAX_CONCURRENCY);

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i++) {
			final int idx = i;
			final String text = chunks.get(i);
			futures.add(CompletableFuture.runAsync(() -> {
				try {
					limiter.acquire();
					byte[] bytes = callWithRetry(() -> synthesizeOne(text, settingOpt, dto), idx);
					synchronized (output) {
						output.write(bytes);
					}
				} catch (Exception e) {
					log.error("❌ TTS 실패 idx={}: {}", idx, e.getMessage());
				} finally {
					limiter.release();
				}
			}, executor));
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		return output.toByteArray();
	}

	private byte[] synthesizeOne(String text, Optional<UserSetting> settingOpt, TTSRequestDTO dto) throws IOException {
		SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
		VoiceSelectionParams voice = buildVoice(settingOpt, dto);
		AudioConfig audio = buildAudio(settingOpt, dto);

		try (TextToSpeechClient client = TextToSpeechClient.create()) {
			SynthesizeSpeechResponse res = client.synthesizeSpeech(input, voice, audio);
			ByteString audioContents = res.getAudioContent();
			return audioContents.toByteArray();
		}
	}

	private VoiceSelectionParams buildVoice(Optional<UserSetting> settingOpt, TTSRequestDTO dto) {
		String gender = Optional.ofNullable(dto != null ? dto.getVoiceName() : null)
				.or(() -> settingOpt.map(UserSetting::getTtsVoiceName))
				.orElse("female")
				.trim()
				.toLowerCase(Locale.ROOT);

		String voiceName = gender.equals("male") || gender.equals("남성")
				? "ko-KR-Standard-D"
				: "ko-KR-Standard-A";

		return VoiceSelectionParams.newBuilder()
				.setLanguageCode("ko-KR")
				.setName(voiceName)
				.setModelName(MODEL)
				.build();
	}

	private AudioConfig buildAudio(Optional<UserSetting> settingOpt, TTSRequestDTO dto) {
		double pitch = Optional.ofNullable(dto != null ? dto.getPitch() : null)
				.or(() -> settingOpt.map(UserSetting::getPitch))
				.orElse(0.0);
		double rate = Optional.ofNullable(dto != null ? dto.getSpeakingRate() : null)
				.or(() -> settingOpt.map(UserSetting::getSpeakingRate))
				.orElse(1.0);

		return AudioConfig.newBuilder()
				.setAudioEncoding(AudioEncoding.MP3)
				.setPitch(pitch)
				.setSpeakingRate(rate)
				.build();
	}

	private List<String> splitIntoChunks(List<String> texts) {
		List<String> result = new ArrayList<>();
		for (String text : texts) {
			if (text == null) continue;
			if (text.length() <= MAX_CHUNK_LEN) result.add(text);
			else for (int i = 0; i < text.length(); i += MAX_CHUNK_LEN)
				result.add(text.substring(i, Math.min(text.length(), i + MAX_CHUNK_LEN)));
		}
		return result;
	}

	private Optional<UserSetting> getUserSetting(CustomUserDetails user) {
		if (user == null || user.getUser() == null) return Optional.empty();
		return userSettingRepository.findByUserId(user.getUser().getId());
	}

	private byte[] callWithRetry(Callable<byte[]> fn, int idx) {
		int retry = 3;
		for (int i = 0; i < retry; i++) {
			try {
				return fn.call();
			} catch (Exception e) {
				log.warn("⚠️ 재시도 {}/{} idx={}, msg={}", i + 1, retry, idx, e.getMessage());
				try { Thread.sleep(700L * (i + 1)); } catch (InterruptedException ignored) {}
			}
		}
		throw new RuntimeException("TTS 재시도 초과 idx=" + idx);
	}

	private String cleanText(String text) {
		if (text == null) return "";
		return text.replaceAll("\\\\n", " ")
				.replaceAll("요약\\s*내용[:：]", "")
				.replaceAll("뉴스\\s*\\d+\\.?\\s*", "")
				.replaceAll("\\s{2,}", " ")
				.trim();
	}
}
