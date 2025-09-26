package com.sync_BE.web.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sync_BE.api.ApiResponse;
import com.sync_BE.service.TokenService.TokenService;
import com.sync_BE.web.dto.TokenResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TokenController {

	private final TokenService tokenService;

	@PostMapping("/reissue")
	@Operation(summary = "토큰 재발급 API", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다.")
	public ApiResponse<TokenResponseDTO> reissue(@RequestBody String refreshToken) {
		TokenResponseDTO responseToken = tokenService.reissueToken(refreshToken);
		return ApiResponse.ok("토큰이 재발급되었습니다.", responseToken);
	}
}
