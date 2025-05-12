package com.sum_news_BE.service.TokenService;

import com.sum_news_BE.web.dto.TokenResponseDTO;

public interface TokenService {
	// 토큰 생성
	TokenResponseDTO generateToken(String userid);

	// 토큰 재발급
	TokenResponseDTO reissueToken(String refreshToken);

	// 리프레시 토큰 저장
	void saveRefreshToken(String userid, String refreshToken);

	// 리프레시 토큰 삭제
	void deleteRefreshToken(String userid);

	// 로그아웃
	void logout(String refreshToken);
}
