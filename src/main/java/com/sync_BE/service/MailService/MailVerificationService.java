package com.sync_BE.service.MailService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class MailVerificationService {
	// 이메일 인증번호 저장 (회원가입과 비밀번호 재설정 모두 사용)
	private final Map<String, Integer> emailAuthMap = new ConcurrentHashMap<>();

	// 회원가입용 인증번호 저장
	public void saveAuthNumber(String email, int authNumber) {
		emailAuthMap.put(email, authNumber);
	}

	// 인증번호 확인 (회원가입과 비밀번호 재설정 모두 사용)
	public boolean verifyAuthNumber(String email, int authNumber) {
		Integer savedAuthNumber = emailAuthMap.get(email);
		if (savedAuthNumber != null && savedAuthNumber == authNumber) {
			return true;
		}
		return false;
	}

	// 인증 완료 후 인증번호 삭제
	public void removeAuthNumber(String email) {
		emailAuthMap.remove(email);
	}
}
