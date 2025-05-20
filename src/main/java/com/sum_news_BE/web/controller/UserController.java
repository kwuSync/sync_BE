package com.sum_news_BE.web.controller;

import org.springframework.web.bind.annotation.*;

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
	public ApiResponse<User> signUp(@Valid @RequestBody UserRequestDTO.JoinDTO joinDTO) {
		User user = userService.joinUser(joinDTO);
		return ApiResponse.ok("회원가입이 완료되었습니다.", user);
	}

	@PostMapping("/login")
	@Operation(summary = "로그인 API", description = "사용자 로그인을 처리하고 JWT 토큰을 발급합니다.")
	public ApiResponse<TokenResponseDTO> login(@Valid @RequestBody UserRequestDTO.LoginDTO loginDTO) {
		User user = userService.login(loginDTO);
		TokenResponseDTO tokenResponse = tokenService.generateToken(user.getUserid());
		return ApiResponse.ok("로그인이 완료되었습니다.", tokenResponse);
	}

	@PostMapping("/logout")
	@Operation(summary = "로그아웃 API", description = "리프레시 토큰을 삭제하여 로그아웃을 처리합니다.")
	public ApiResponse<Void> logout(@RequestBody String refreshToken) {
		refreshToken = refreshToken.replaceAll("^\"|\"$", "");
		tokenService.logout(refreshToken);
		return ApiResponse.ok("로그아웃이 완료되었습니다.", null);
	}

	@DeleteMapping("/delete")
	@Operation(summary = "회원탈퇴 API", description = "사용자를 삭제합니다.")
	public ApiResponse<User> delete(@Valid @RequestBody UserRequestDTO.DeleteDTO deleteDTO) {
		User user = userService.delete(deleteDTO.getUserid());
		return ApiResponse.ok("회원탈퇴가 완료되었습니다.", user);
	}

	@PatchMapping("/update")
	@Operation(summary = "사용자 정보 수정 API", description = "사용자의 비밀번호나 이름을 부분적으로 수정합니다.")
	public ApiResponse<User> update(@Valid @RequestBody UserRequestDTO.UpdateDTO updateDTO) {
		User user = userService.update(updateDTO);
		return ApiResponse.ok("사용자 정보가 수정되었습니다.", user);
	}
}
