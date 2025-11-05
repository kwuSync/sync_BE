package com.sync_BE.web.controller;

import java.io.IOException;

import com.sync_BE.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.sync_BE.security.CustomUserDetails;
import com.sync_BE.service.TTSService.TTSService;
import com.sync_BE.web.dto.TTSRequestDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Slf4j
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

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ApiResponse<Void>> handleIOException(IOException e) {

		// "Broken pipe" 오류는 클라이언트가 정상적으로 연결을 끊은 경우(페이지 이탈 등)이므로,
		// 에러 로그 대신 경고(warn) 로그만 남깁니다.
		if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
			log.warn("TTS 스트리밍 중 클라이언트가 연결을 끊었습니다: {}", e.getMessage());
			// 클라이언트가 이미 떠났으므로 아무것도 반환하지 않습니다.
			return null;
		}

		// 그 외의 IO 오류는 에러로 기록합니다.
		log.error("TTS 오디오 생성 중 오류 발생: {}", e.getMessage());

		String errorMessage = "TTS 오디오 생성 중 오류가 발생했습니다: " + e.getMessage();
		ApiResponse<Void> errorResponse = ApiResponse.error("500", errorMessage);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON); // Content-Type을 JSON으로 명시

		return new ResponseEntity<>(errorResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
