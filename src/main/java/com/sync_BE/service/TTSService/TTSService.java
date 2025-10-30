package com.sync_BE.service.TTSService;

import java.io.IOException;

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

@Service
public class TTSService {

	private final NewsService newsService;

	public TTSService(NewsService newsService) {
		this.newsService = newsService;
	}

	public byte[] synthesizeMainSummary() throws IOException {
		String summaryText = newsService.getNewsSummary();
		return synthesize(summaryText);
	}

	public byte[] synthesizeNewsSummary(String articleId) throws IOException {
		Optional<>
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
