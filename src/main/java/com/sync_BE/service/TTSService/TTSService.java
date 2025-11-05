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

		return synthesize(summaryText, settingOpt);
	}

	public byte[] synthesizeNewsSummary(String clusterId, CustomUserDetails userDetails) throws IOException {
		NewsResponseDTO.NewsClusterDTO newsCluster = newsService.getNewsSummaryByClusterId(clusterId);
		if (newsCluster == null || newsCluster.getSummary() == null || newsCluster.getSummary().getArticle() == null) { //
			throw new IOException("요약된 뉴스를 찾을 수 없습니다.");
		}
		String summaryText = newsCluster.getSummary().getArticle();

		Optional<UserSetting> settingOpt = getUserSetting(userDetails);
		if (settingOpt.isPresent() && !settingOpt.get().isTtsEnabled()) {
			throw new IOException("사용자가 TTS 기능을 비활성화했습니다.");
		}
		return synthesize(summaryText, settingOpt);
	}

	private byte[] synthesize(String text, Optional<UserSetting> settingOpt) throws IOException {
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
			SynthesisInput input = SynthesisInput.newBuilder()
				.setText(text)
				.build();

			VoiceSelectionParams.Builder voiceBuilder = VoiceSelectionParams.newBuilder();
			voiceBuilder.setLanguageCode("ko-KR");


			String voiceName = settingOpt.map(UserSetting::getTtsVoiceName).orElse(null);

			if ("MALE".equalsIgnoreCase(voiceName)) {
				voiceBuilder.setSsmlGender(SsmlVoiceGender.MALE);
			}
			else if ("FEMALE".equalsIgnoreCase(voiceName)) {

				voiceBuilder.setSsmlGender(SsmlVoiceGender.FEMALE);
			}
			else if (voiceName != null && !voiceName.isEmpty()) {
				voiceBuilder.setName(voiceName);
			}
			else {
				voiceBuilder.setSsmlGender(SsmlVoiceGender.FEMALE);
			}

			VoiceSelectionParams voice = voiceBuilder.build();

			AudioConfig.Builder audioBuilder = AudioConfig.newBuilder()
				.setAudioEncoding(AudioEncoding.MP3);

			if (settingOpt.isPresent()) {
				UserSetting setting = settingOpt.get();
				if (setting.getPitch() != null) {
					audioBuilder.setPitch(setting.getPitch());
				}
				if (setting.getSpeakingRate() != null) {
					audioBuilder.setSpeakingRate(setting.getSpeakingRate());
				}
			}

			AudioConfig audioConfig = audioBuilder.build();

			SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
			ByteString audioContents = response.getAudioContent();
			return audioContents.toByteArray();
		}
	}
}
