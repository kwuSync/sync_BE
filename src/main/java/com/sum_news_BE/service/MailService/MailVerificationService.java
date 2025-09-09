package com.sum_news_BE.service.MailService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class MailVerificationService {
	private final Map<String, Integer> emailAuthMap = new ConcurrentHashMap<>();

	// 인증번호 저장
	public void saveAuthNumber(String email, int authNumber) {
		emailAuthMap.put(email, authNumber);
	}

	// 인증번호 확인
	public boolean verifyAuthNumber(String email, int authNumber) {
		Integer savedAuthNumber = emailAuthMap.get(email);
		if (savedAuthNumber != null && savedAuthNumber == authNumber) {
			emailAuthMap.remove(email);
			return true;
		}
		return false;
	}
}
