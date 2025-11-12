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

	private static final int MAX_CONCURRENCY = 3;
	private static final int MAX_CHUNK_LEN = 3000;
	private static final String MODEL = "gemini-2.5-pro-tts";

	private final NewsService newsService;
	private final UserSettingRepository userSettingRepository;
	private final ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENCY);

	public TTSService(NewsService newsService, UserSettingRepository userSettingRepository) {
		this.newsService = newsService;
		this.userSettingRepository = userSettingRepository;
	}

	public byte[] synthesizeMainSummary(CustomUserDetails user, TTSRequestDTO dto, int page, int pageSize) throws IOException {
		var mainNews = newsService.getMain();
		if (mainNews == null || mainNews.getNewsList().isEmpty()) {
			throw new IOException("요약 뉴스가 없습니다.");
		}

		List<String> allTexts = mainNews.getNewsList().stream()
				.map(NewsResponseDTO.NewsArticleDTO::getSummaryText)
				.filter(t -> t != null && !t.isBlank())
				.collect(Collectors.toList());

		int from = Math.max(0, (page - 1) * pageSize);
		int to = Math.min(from + pageSize, allTexts.size());
		if (from >= allTexts.size()) throw new IOException("요청한 페이지의 기사가 없습니다.");

		List<String> pageTexts = allTexts.subList(from, to);
		log.info("🎧 TTS 요청: page={} (기사 {}~{})", page, from + 1, to);
		return synthesizeTexts(pageTexts, user, dto);
	}

	public byte[] synthesizeDirectText(CustomUserDetails user, String fullText, TTSRequestDTO dto, int page, int pageSize) throws IOException {
		if (fullText == null || fullText.isBlank()) throw new IOException("텍스트가 비어있습니다.");

		String[] splitArticles = fullText.split("뉴스\\s*\\d+\\.?");

		List<String> articles = Arrays.stream(splitArticles)
				.map(String::trim)
				.filter(t -> !t.isBlank())
				.collect(Collectors.toList());

		int from = Math.max(0, (page - 1) * pageSize);
		int to = Math.min(from + pageSize, articles.size());
		if (from >= articles.size()) throw new IOException("요청한 페이지의 뉴스가 없습니다.");

		List<String> pageArticles = articles.subList(from, to);

		log.info("🎧 직접 text 기반 TTS 요청: page={} (기사 {}~{})", page, from + 1, to);
		return synthesizeTexts(pageArticles, user, dto);
	}

	public byte[] synthesizeNewsSummary(String clusterId, CustomUserDetails user, TTSRequestDTO dto) throws IOException {
		var cluster = newsService.getNewsSummaryByClusterId(clusterId);
		if (cluster == null || cluster.getSummary() == null) {
			throw new IOException("클러스터 요약을 찾을 수 없습니다.");
		}

		String article = cluster.getSummary().getArticle();
		return synthesizeTexts(List.of(article), user, dto);
	}

	private byte[] synthesizeTexts(List<String> texts, CustomUserDetails user, TTSRequestDTO dto) throws IOException {
		Optional<UserSetting> settingOpt = getUserSetting(user);
		List<String> chunks = splitIntoChunks(texts);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Semaphore limiter = new Semaphore(MAX_CONCURRENCY);

		List<CompletableFuture<Void>> tasks = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i++) {
			final String text = chunks.get(i);
			final int idx = i;
			tasks.add(CompletableFuture.runAsync(() -> {
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

		CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
		return output.toByteArray();
	}

	private byte[] synthesizeOne(String text, Optional<UserSetting> settingOpt, TTSRequestDTO dto) throws IOException {
		SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
		VoiceSelectionParams voice = buildVoice(settingOpt, dto);
		AudioConfig config = buildAudio(settingOpt, dto);

		try (TextToSpeechClient client = TextToSpeechClient.create()) {
			SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, config);
			ByteString audio = response.getAudioContent();
			return audio.toByteArray();
		}
	}

	private VoiceSelectionParams buildVoice(Optional<UserSetting> settingOpt, TTSRequestDTO dto) {
		String voiceName = Optional.ofNullable(dto != null ? dto.getVoiceName() : null)
				.or(() -> settingOpt.map(UserSetting::getTtsVoiceName))
				.orElse("FEMALE");

		String normalized = voiceName.trim().toUpperCase(Locale.ROOT);
		String resolved;

		switch(normalized) {
			case "MALE":
			case "M":
			case "남성":
				resolved = "Alnilam";
				break;
			default:
				resolved = "Achernar";
				break;
		}

		return VoiceSelectionParams.newBuilder()
				.setLanguageCode("ko-KR")
				.setName(resolved)
				.build();
	}

	private AudioConfig buildAudio(Optional<UserSetting> settingOpt, TTSRequestDTO dto) {
		Double pitch = Optional.ofNullable(dto != null ? dto.getPitch() : null)
				.or(() -> settingOpt.map(UserSetting::getPitch))
				.orElse(0.0);

		Double rate = Optional.ofNullable(dto != null ? dto.getSpeakingRate() : null)
				.or(() -> settingOpt.map(UserSetting::getSpeakingRate))
				.orElse(1.0);

		return AudioConfig.newBuilder()
				.setAudioEncoding(AudioEncoding.MP3)
				.setPitch(pitch)
				.setSpeakingRate(rate)
				.build();
	}

	private List<String> splitIntoChunks(List<String> texts) {
		List<String> chunks = new ArrayList<>();
		for (String text : texts) {
			if (text.length() <= MAX_CHUNK_LEN) chunks.add(text);
			else for (int i = 0; i < text.length(); i += MAX_CHUNK_LEN)
				chunks.add(text.substring(i, Math.min(text.length(), i + MAX_CHUNK_LEN)));
		}
		return chunks;
	}

	private Optional<UserSetting> getUserSetting(CustomUserDetails user) {
		if (user == null || user.getUser() == null) return Optional.empty();
		return userSettingRepository.findByUserId(user.getUser().getId());
	}

	private byte[] callWithRetry(Callable<byte[]> fn, int idx) {
		int retry = 3;
		long wait = 800L;
		for (int i = 0; i < retry; i++) {
			try {
				return fn.call();
			} catch (Exception e) {
				log.warn("⚠️ 재시도 {}/{} idx={}, msg={}", i + 1, retry, idx, e.getMessage());
				try {
					Thread.sleep(wait);
				} catch (InterruptedException ignored) {}
			}
		}
		throw new RuntimeException("TTS 재시도 초과 idx=" + idx);
	}

	private String preprocessTextForSpeech(String text) {
		if (text == null) return "";

		String cleaned = text;

		cleaned = cleaned.replaceAll("\\\\n", ", ");
		cleaned = cleaned.replaceAll("\\n", ", ");

		cleaned = cleaned.replaceAll("(?i)요약\\s*내용\\s*[:：]", "");
		cleaned = cleaned.replaceAll("뉴스\\s*\\d+\\s*\\.?", "");

		cleaned = cleaned.replaceAll("[-•·]+\\s*", "");

		cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();

		if (!cleaned.endsWith(".")) cleaned += ".";

		return cleaned;
	}
}
