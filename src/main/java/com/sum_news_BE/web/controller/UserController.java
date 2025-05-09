package com.sum_news_BE.web.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sum_news_BE.api.ApiResponse;
import com.sum_news_BE.domain.User;
import com.sum_news_BE.service.UserService.UserCommandService;
import com.sum_news_BE.service.UserService.UserQueryService;
import com.sum_news_BE.web.dto.UserRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	private final UserCommandService userCommandService;
	private final UserQueryService userQueryService;

	@PostMapping("/signup")
	@Operation(summary = "회원가입 API", description = "새로운 사용자를 등록합니다.")
	public ApiResponse<String> signUp(@RequestBody @Valid UserRequestDTO.JoinDTO joinDTO) {
		User user = userCommandService.joinUser(joinDTO);
		return ApiResponse.ok("회원가입이 완료되었습니다.", user);
	}

}
