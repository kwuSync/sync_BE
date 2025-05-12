package com.sum_news_BE.web.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sum_news_BE.api.ApiResponse;
import com.sum_news_BE.domain.User;
import com.sum_news_BE.service.TokenService.TokenService;
import com.sum_news_BE.service.UserService.UserService;
import com.sum_news_BE.web.dto.TokenResponseDTO;
import com.sum_news_BE.web.dto.UserRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	private final UserService userService;
	private final TokenService tokenService;

	@PostMapping("/signup")
	@Operation(summary = "회원가입 API", description = "새로운 사용자를 등록합니다.")
	public ApiResponse<User> signUp(@RequestBody @Valid UserRequestDTO.JoinDTO joinDTO) {
		User user = userService.joinUser(joinDTO);
		return ApiResponse.ok("회원가입이 완료되었습니다.", user);
	}

	@PostMapping("/login")
	@Operation(summary = "로그인 API", description = "사용자 로그인을 처리하고 JWT 토큰을 발급합니다.")
	public ApiResponse<TokenResponseDTO> login(@RequestBody @Valid UserRequestDTO.LoginDTO loginDTO) {
		User user = userService.login(loginDTO);
		TokenResponseDTO tokenResponse = tokenService.generateToken(user.getUserid());
		return ApiResponse.ok("로그인이 완료되었습니다.", tokenResponse);
	}
}
