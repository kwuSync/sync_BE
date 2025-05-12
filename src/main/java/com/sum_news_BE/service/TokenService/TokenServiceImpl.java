package com.sum_news_BE.service.TokenService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sum_news_BE.domain.RefreshToken;
import com.sum_news_BE.repository.RefreshTokenRepository;
import com.sum_news_BE.security.JwtProvider;
import com.sum_news_BE.web.dto.TokenResponseDTO;
import com.sum_news_BE.domain.User;
import com.sum_news_BE.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

	private final JwtProvider jwtProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private static final Logger log = LoggerFactory.getLogger(TokenServiceImpl.class);

	@Override
	public TokenResponseDTO generateToken(String userid) {
		System.out.println("토큰 생성 시도: " + userid);
		String accessToken = jwtProvider.generateAccessToken(userid);
		System.out.println("AccessToken 생성 성공");
		String refreshToken = jwtProvider.generateRefreshToken(userid);
		System.out.println("RefreshToken 생성 성공");

		// 리프레시 토큰 저장
		saveRefreshToken(userid, refreshToken);
		System.out.println("RefreshToken 저장 성공");

		return TokenResponseDTO.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	@Override
	public TokenResponseDTO reissueToken(String refreshToken) {
		try {
			log.info("토큰 재발급 시도: {}", refreshToken);
			
			// 리프레시 토큰 유효성 검증
			if (!jwtProvider.validateToken(refreshToken)) {
				log.error("리프레시 토큰 유효성 검증 실패");
				throw new RuntimeException("리프레시 토큰이 유효하지 않습니다.");
			}

			// 리프레시 토큰으로 사용자 ID 추출
			String userid = jwtProvider.getUseridFromToken(refreshToken);
			log.info("리프레시 토큰에서 추출한 사용자 ID: {}", userid);

			// 기존 토큰 삭제
			refreshTokenRepository.deleteByUserid(userid);
			log.info("기존 리프레시 토큰 삭제 완료");

			// 사용자 정보 조회
			User user = userRepository.findByUserid(userid)
				.orElseThrow(() -> {
					log.error("사용자를 찾을 수 없음: {}", userid);
					return new RuntimeException("사용자를 찾을 수 없습니다.");
				});
			log.info("사용자 조회 성공: {}", userid);

			// 새로운 토큰 생성
			String newAccessToken = jwtProvider.generateAccessToken(userid);
			String newRefreshToken = jwtProvider.generateRefreshToken(userid);
			log.info("새로운 액세스 토큰 생성: {}", newAccessToken);
			log.info("새로운 리프레시 토큰 생성: {}", newRefreshToken);

			// DB에 새로운 리프레시 토큰 저장
			RefreshToken newToken = new RefreshToken(userid, newRefreshToken);
			refreshTokenRepository.save(newToken);
			log.info("새로운 리프레시 토큰 저장 성공");

			return TokenResponseDTO.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.build();
		} catch (Exception e) {
			log.error("토큰 재발급 중 에러 발생: {}", e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void saveRefreshToken(String userid, String refreshToken) {
		RefreshToken token = new RefreshToken(userid, refreshToken);
		refreshTokenRepository.save(token);
	}

	@Override
	public void deleteRefreshToken(String userid) {
		refreshTokenRepository.deleteByUserid(userid);
	}

	@Override
	public void logout(String refreshToken) {
		log.info("로그아웃 시도: {}", refreshToken);
		String userid = jwtProvider.getUseridFromToken(refreshToken);
		refreshTokenRepository.deleteByUserid(userid);
		log.info("로그아웃 완료: {}", userid);
	}

}
