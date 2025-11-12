package com.sync_BE.web.controller;

import java.io.IOException;

import com.sync_BE.api.ApiResponse;
import com.sync_BE.security.CustomUserDetails;
import com.sync_BE.service.TTSService.TTSService;
import com.sync_BE.web.dto.TTSRequestDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "TTS", description = "TTS 재생 관련 API")
public class TTSController {

	private final TTSService ttsService;

	@PostMapping("/main/tts")
	public ResponseEntity<byte[]> mainSummary(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "5") int pageSize,
			@RequestBody(required = false) TTSRequestDTO ttsRequestDTO
	) throws IOException {

		byte[] audioContent = ttsService.synthesizeMainSummary(userDetails, ttsRequestDTO, page, pageSize);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("audio/mpeg"));
		headers.set("Content-Disposition", "inline; filename=\"main-summary-page" + page + ".mp3\"");
		headers.setCacheControl("no-cache, no-store, must-revalidate");

		return ResponseEntity.ok().headers(headers).body(audioContent);
	}

	@PostMapping("/cluster/{clusterId}/tts")
	public ResponseEntity<byte[]> newsSummary(
			@PathVariable String clusterId,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody(required = false) TTSRequestDTO ttsRequestDTO
	) throws IOException {

		byte[] audioContent = ttsService.synthesizeNewsSummary(clusterId, userDetails, ttsRequestDTO);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("audio/mpeg"));
		headers.set("Content-Disposition", "inline; filename=\"news-" + clusterId + ".mp3\"");
		headers.setCacheControl("no-cache, no-store, must-revalidate");

		return ResponseEntity.ok().headers(headers).body(audioContent);
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ApiResponse<Void>> handleIOException(IOException e) {

		if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
			log.warn("TTS 스트리밍 중 클라이언트가 연결 종료: {}", e.getMessage());
			return null;
		}

		log.error("❌ TTS 오디오 생성 중 오류 발생: {}", e.getMessage());
		String errorMessage = "TTS 오디오 생성 중 오류가 발생했습니다: " + e.getMessage();

		ApiResponse<Void> errorResponse = ApiResponse.error("500", errorMessage);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<>(errorResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
