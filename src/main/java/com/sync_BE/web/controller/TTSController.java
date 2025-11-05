package com.sync_BE.web.controller;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.sync_BE.security.CustomUserDetails;
import com.sync_BE.service.TTSService.TTSService;
import com.sync_BE.web.dto.TTSRequestDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "TTS", description = "TTS 재생 관련 API")
public class TTSController {

	private final TTSService ttsService;

	@PostMapping("/main/tts")  // 메인 화면 뉴스 TTS
	public ResponseEntity<byte[]> mainSummary(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody(required = false)
		TTSRequestDTO ttsRequestDTO) throws IOException {
		byte[] audioContent = ttsService.synthesizeMainSummary(userDetails, ttsRequestDTO);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.set("Content-Disposition", "inline; filename=\"main-summary.mp3\"");
		return ResponseEntity.ok().headers(headers).body(audioContent);
	}

	@PostMapping("/cluster/{clusterId}/tts")  // 상세 요약 뉴스 TTS
	public ResponseEntity<byte[]> newsSummary(@PathVariable String clusterId, @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody(required = false) TTSRequestDTO ttsRequestDTO) throws IOException {
		byte[] audioContent = ttsService.synthesizeNewsSummary(clusterId, userDetails, ttsRequestDTO);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.set("Content-Disposition", "inline; filename=\"news-" + clusterId + ".mp3\"");
		return ResponseEntity.ok().headers(headers).body(audioContent);
	}
}
