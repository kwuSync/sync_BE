package com.sum_news_BE.web.controller;

import com.sum_news_BE.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sum_news_BE.service.MailService.MailVerificationService;
import com.sum_news_BE.service.MailService.MailService;
import com.sum_news_BE.web.dto.MailRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
@Tag(name = "mail", description = "이메일 인증 관련 API")
public class MailController {
	private final MailService mailService;
	private final MailVerificationService mailVerificationService;

	@PostMapping("/send")
	@Operation(summary = "이메일 인증번호 전송", description = "입력한 이메일로 인증번호를 전송합니다.")
	public ApiResponse<Void> sendMail(@RequestBody MailRequestDTO.SendDTO request) {
		int authNumber = mailService.sendMail(request.getEmail());
		return ApiResponse.ok("인증 메일이 성공적으로 발송되었습니다.", null);
	}

	@PostMapping("/verify")
	@Operation(summary = "이메일 인증번호 확인", description = "입력한 인증번호가 올바른지 확인합니다.")
	public ApiResponse<Boolean> verifyMail(@RequestBody MailRequestDTO.VerifyDTO request) {
		boolean isVerified = mailVerificationService.verifyAuthNumber(request.getEmail(), request.getAuthNumber());
		return ApiResponse.ok("인증번호 확인이 완료되었습니다.", isVerified);
	}
}
