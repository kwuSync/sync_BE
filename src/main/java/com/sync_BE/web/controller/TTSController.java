package com.sync_BE.web.controller;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sync_BE.service.TTSService.TTSService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TTSController {

	private final TTSService ttsService;

	@GetMapping("/main/tts")  // 메인 화면 뉴스 TTS
	public ResponseEntity<byte[]> mainSummary() throws IOException {
		byte[] audioContent = ttsService.synthesizeMainSummary();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.set("Content-Disposition", "inline; filename=\"main-summary.mp3\"");
		return ResponseEntity.ok().headers(headers).body(audioContent);
	}

	@GetMapping("/cluster/{clusterId}/tts")  // 상세 요약 뉴스 TTS
	public ResponseEntity<byte[]> newsSummary(@PathVariable String clusterId) throws IOException {
		byte[] audioContent = ttsService.synthesizeNewsSummary(clusterId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.set("Content-Disposition", "inline; filename=\"news-" + clusterId + ".mp3\"");
		return ResponseEntity.ok().headers(headers).body(audioContent);
	}
}
