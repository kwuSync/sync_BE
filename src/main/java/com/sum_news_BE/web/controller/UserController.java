package com.sum_news_BE.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sum_news_BE.domain.User;
import com.sum_news_BE.service.UserService.UserService;
import com.sum_news_BE.service.TokenService.TokenService;
import com.sum_news_BE.web.dto.UserRequestDTO;
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
	public ResponseEntity<Void> join(@Valid @RequestBody UserRequestDTO.JoinDTO joinDTO) {
		userService.join(joinDTO);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
	@PostMapping("/login")
	public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody UserRequestDTO.LoginDTO loginDTO) {
		User user = userService.login(loginDTO);
		TokenResponseDTO tokenResponse = tokenService.generateToken(user.getEmail());
		return ResponseEntity.ok(tokenResponse);
	}

	@Operation(summary = "사용자 정보 조회", description = "이메일로 사용자 정보를 조회합니다.")
	@GetMapping
	public ResponseEntity<User> getUserInfo(String email) {
		User user = userService.getUserByEmail(email);
		return ResponseEntity.ok(user);
	}

	@Operation(summary = "회원 탈퇴", description = "이메일과 비밀번호 확인 후 회원 탈퇴를 처리합니다.")
	@DeleteMapping
	public ResponseEntity<Void> delete(@Valid @RequestBody UserRequestDTO.DeleteDTO deleteDTO) {
		userService.delete(deleteDTO);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "회원 정보 수정", description = "이메일로 사용자를 찾아 정보를 수정합니다.")
	@PutMapping
	public ResponseEntity<User> update(String email, @Valid @RequestBody UserRequestDTO.UpdateDTO updateDTO) {
		User updatedUser = userService.update(email, updateDTO);
		return ResponseEntity.ok(updatedUser);
	}

	@Operation(summary = "로그아웃", description = "사용자의 리프레시 토큰을 삭제하여 로그아웃을 처리합니다.")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String refreshToken) {
		tokenService.logout(refreshToken);
		return ResponseEntity.ok().build();
	}
}
