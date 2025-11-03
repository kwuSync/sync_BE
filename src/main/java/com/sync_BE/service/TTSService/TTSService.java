package com.sync_BE.service.TTSService;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import com.sync_BE.domain.UserSetting;
import com.sync_BE.repository.UserSettingRepository;
import com.sync_BE.security.CustomUserDetails;
import com.sync_BE.service.NewsService.NewsService;
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

	public byte[] synthesizeMainSummary(CustomUserDetails userDetails) throws IOException {
		NewsResponseDTO.NewsListDTO mainNewsList = newsService.getMain();

		if (mainNewsList == null || mainNewsList.getNewsList() == null || mainNewsList.getNewsList().isEmpty()) {
			throw new IOException("메인 요약 뉴스를 찾을 수 없습니다.");
		}
		NewsResponseDTO.NewsArticleDTO firstArticle = mainNewsList.getNewsList().get(0);
		String summaryText = firstArticle.getSummaryText();

		Optional<UserSetting> settingOpt = getUserSetting(userDetails);

		if (settingOpt.isPresent() && !settingOpt.get().isTtsEnabled()) {
			throw new IOException("사용자가 TTS 기능을 비활성화했습니다.");
		}
		String voiceName = settingOpt.map(UserSetting::getTtsVoice).orElse(null);
		return synthesize(summaryText, voiceName);
	}

	public byte[] synthesizeNewsSummary(String clusterId, CustomUserDetails userDetails) throws IOException {
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
			throw new IOException("사용자가 TTS 기능을 비활성화했습니다.");
		}
		String voiceName = settingOpt.map(UserSetting::getTtsVoice).orElse(null);
		return synthesize(summaryText, voiceName);
	}

	private byte[] synthesize(String text, String userVoiceName) throws IOException {
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
			SynthesisInput input = SynthesisInput.newBuilder()
				.setText(text)
				.build();

			VoiceSelectionParams.Builder voiceBuilder = VoiceSelectionParams.newBuilder();

			if (userVoiceName != null && !userVoiceName.isEmpty()) {
				voiceBuilder.setName(userVoiceName);
				if (userVoiceName.startsWith("ko-KR")) {
					voiceBuilder.setLanguageCode("ko-KR");
				}
			} else {
				voiceBuilder.setLanguageCode("ko-KR")
					.setSsmlGender(SsmlVoiceGender.FEMALE);
			}
			VoiceSelectionParams voice = voiceBuilder.build();

			AudioConfig audioConfig = AudioConfig.newBuilder()
				.setAudioEncoding(AudioEncoding.MP3)
				.build();
			SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
			ByteString audioContents = response.getAudioContent();
			return audioContents.toByteArray();
		}
	}
}
