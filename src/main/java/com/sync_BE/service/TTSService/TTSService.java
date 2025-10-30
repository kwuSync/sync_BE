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
import com.sync_BE.service.NewsService.NewsService;
import com.sync_BE.web.dto.newsDTO.NewsResponseDTO;

@Service
public class TTSService {

	private final NewsService newsService;

	public TTSService(NewsService newsService) {
		this.newsService = newsService;
	}

	public byte[] synthesizeMainSummary() throws IOException {
		NewsResponseDTO.NewsListDTO mainNewsList = newsService.getMain();

		if (mainNewsList == null || mainNewsList.getNewsList() == null || mainNewsList.getNewsList().isEmpty()) {
			throw new IOException("메인 요약 뉴스를 찾을 수 없습니다.");
		}
		NewsResponseDTO.NewsArticleDTO firstArticle = mainNewsList.getNewsList().get(0);

		String summaryText = firstArticle.getSummaryText();
		return synthesize(summaryText);
	}

	public byte[] synthesizeNewsSummary(String clusterId) throws IOException {
		NewsResponseDTO.NewsClusterDTO newsCluster = newsService.getNewsSummaryByClusterId(clusterId);
		if (newsCluster == null || newsCluster.getSummary() == null || newsCluster.getSummary().getArticle() == null) { //
			throw new IOException("요약된 뉴스를 찾을 수 없습니다.");
		}
		String summaryText = newsCluster.getSummary().getArticle();

		if (summaryText.isEmpty()) {
			throw new IOException("요약 텍스트가 비어있습니다.");
		}
		return synthesize(summaryText);
	}

	private byte[] synthesize(String text) throws IOException {
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
			SynthesisInput input = SynthesisInput.newBuilder()
				.setText(text)
				.build();

			VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
				.setLanguageCode("ko-KR")
				.setSsmlGender(SsmlVoiceGender.FEMALE)
				.build();

			AudioConfig audioConfig = AudioConfig.newBuilder()
				.setAudioEncoding(AudioEncoding.MP3)
				.build();
			SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
			ByteString audioContents = response.getAudioContent();
			return audioContents.toByteArray();
		}
	}
}
