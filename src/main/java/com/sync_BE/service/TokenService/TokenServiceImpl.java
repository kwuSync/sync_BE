package com.sync_BE.service.TokenService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sync_BE.domain.RefreshToken;
import com.sync_BE.domain.User;
import com.sync_BE.repository.RefreshTokenRepository;
import com.sync_BE.repository.UserRepository;
import com.sync_BE.security.JwtProvider;
import com.sync_BE.web.dto.TokenResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

	private final JwtProvider jwtProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final RedisTokenBlacklistService redisTokenBlacklistService;

	@Override
	public TokenResponseDTO generateToken(String email) {
		log.info("토큰 생성 시도: {}", email);
		String accessToken = jwtProvider.generateAccessToken(email);
		log.info("AccessToken 생성 성공");

		String refreshToken = jwtProvider.generateRefreshToken(email);
		log.info("RefreshToken 생성 성공");

		saveRefreshToken(email, refreshToken);

		return TokenResponseDTO.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();
	}

	@Override
	@Transactional
	public TokenResponseDTO reissueToken(String refreshToken) {
		if (!jwtProvider.validateToken(refreshToken)) {
			throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
		}

		String email = jwtProvider.getEmailFromToken(refreshToken);
		log.info("리프레시 토큰에서 추출한 이메일: {}", email);

		deleteRefreshToken(email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.error("사용자를 찾을 수 없음: {}", email);
					return new IllegalArgumentException("존재하지 않는 사용자입니다.");
				});
		log.info("사용자 조회 성공: {}", email);

		String newAccessToken = jwtProvider.generateAccessToken(email);
		String newRefreshToken = jwtProvider.generateRefreshToken(email);

		saveRefreshToken(email, newRefreshToken);

		return TokenResponseDTO.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.build();
	}

	@Override
	@Transactional
	public void saveRefreshToken(String email, String refreshToken) {
		refreshTokenRepository.findByEmail(email)
				.ifPresent(refreshTokenRepository::delete);

		RefreshToken newRefreshToken = RefreshToken.builder()
				.email(email)
				.refreshToken(refreshToken)
				.build();
		refreshTokenRepository.save(newRefreshToken);
		log.info("리프레시 토큰 저장 완료: {}", email);
	}

	@Override
	@Transactional
	public void deleteRefreshToken(String email) {
		refreshTokenRepository.findByEmail(email)
				.ifPresent(refreshTokenRepository::delete);
		log.info("리프레시 토큰 삭제 완료: {}", email);
	}

	@Override
	@Transactional
	public void logout(String refreshToken) {
		if (!jwtProvider.validateToken(refreshToken)) {
			throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
		}

		String email = jwtProvider.getEmailFromToken(refreshToken);
		deleteRefreshToken(email);
		redisTokenBlacklistService.blacklistToken(refreshToken);
		
		log.info("로그아웃 완료: {} (토큰 블랙리스트 추가)", email);
	}

	@Override
	public String resolveToken(String bearerToken) {
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
