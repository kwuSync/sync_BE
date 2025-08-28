package com.sum_news_BE.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sum_news_BE.api.ApiResponse;
import com.sum_news_BE.domain.User;
import com.sum_news_BE.service.UserService.UserService;
import com.sum_news_BE.service.TokenService.TokenService;
import com.sum_news_BE.web.dto.userDTO.UserRequestDTO;
import com.sum_news_BE.web.dto.TokenResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final TokenService tokenService;

	@Operation(summary = "회원가입", description = "이메일 인증이 완료된 사용자의 회원가입을 처리합니다.")
	@PostMapping("/join")
	public ApiResponse<Void> join(@Valid @RequestBody UserRequestDTO.JoinDTO joinDTO) {
		userService.join(joinDTO);
		return ApiResponse.ok("회원가입이 완료되었습니다.", null);
	}

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
	@PostMapping("/login")
	public ApiResponse<TokenResponseDTO> login(@Valid @RequestBody UserRequestDTO.LoginDTO loginDTO) {
		User user = userService.login(loginDTO);
		TokenResponseDTO tokenResponse = tokenService.generateToken(user.getEmail());
		return ApiResponse.ok("로그인에 성공했습니다.", tokenResponse);
	}

	@Operation(summary = "사용자 정보 조회", description = "이메일로 사용자 정보를 조회합니다.")
	@GetMapping
	public ApiResponse<User> getUserInfo(String email) {
		User user = userService.getUserByEmail(email);
		return ApiResponse.ok("사용자 정보 조회가 완료되었습니다.", user);
	}

	@Operation(summary = "비밀번호 찾기", description = "이메일로 비밀번호 재설정 인증번호를 전송합니다.")
	@PostMapping("/password/reset")
	public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody UserRequestDTO.PasswordResetRequestDTO requestDTO) {
		userService.requestPasswordReset(requestDTO);
		return ApiResponse.ok("비밀번호 재설정 메일이 전송되었습니다.", null);
	}

	@Operation(summary = "비밀번호 재설정 확인", description = "인증번호 확인 후 새 비밀번호로 변경합니다.")
	@PostMapping("/password/confirm")
	public ApiResponse<Void> confirmPasswordReset(@Valid @RequestBody UserRequestDTO.PasswordResetConfirmDTO confirmDTO) {
		userService.confirmPasswordReset(confirmDTO);
		return ApiResponse.ok("비밀번호가 성공적으로 재설정되었습니다.", null);
	}

	@Operation(summary = "회원 탈퇴", description = "이메일과 비밀번호 확인 후 회원 탈퇴를 처리합니다.")
	@DeleteMapping
	public ApiResponse<Void> delete(@Valid @RequestBody UserRequestDTO.DeleteDTO deleteDTO) {
		userService.delete(deleteDTO);
		return ApiResponse.ok("회원 탈퇴가 완료되었습니다.", null);
	}

	@Operation(summary = "회원 정보 수정", description = "이메일로 사용자를 찾아 정보를 수정합니다.")
	@PatchMapping
	public ApiResponse<User> update(String email, @Valid @RequestBody UserRequestDTO.UpdateDTO updateDTO) {
		User updatedUser = userService.update(email, updateDTO);
		return ApiResponse.ok("회원 정보가 수정되었습니다.", updatedUser);
	}

	@Operation(summary = "로그아웃", description = "사용자의 리프레시 토큰을 삭제하고 accessToken을 블랙리스트에 추가하여 로그아웃을 처리합니다.")
	@PostMapping("/logout")
	public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
		String refreshToken = authorization;
		if (authorization.startsWith("Bearer ")) {
			refreshToken = authorization.substring(7);
		}
		
		tokenService.logout(refreshToken);
		return ApiResponse.ok("로그아웃 되었습니다.", null);
	}
}
