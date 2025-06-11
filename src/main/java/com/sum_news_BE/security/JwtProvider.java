package com.sum_news_BE.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.sum_news_BE.repository.RefreshTokenRepository;
import com.sum_news_BE.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {

	@Value("${jwt.secret}")
	private String secretKey;

	@Value("${jwt.access-expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-expiration}")
	private long refreshTokenExpiration;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes());
	}

	public String generateAccessToken(String email) {
		log.info("AccessToken 생성: {}", email);
		return Jwts.builder()
				.setSubject(email)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public String generateRefreshToken(String email) {
		log.info("RefreshToken 생성 시도: {}", email);
		try {
			String token = Jwts.builder()
					.setSubject(email)
					.setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
					.signWith(getSigningKey(), SignatureAlgorithm.HS256)
					.compact();
			log.info("RefreshToken 생성 성공: {}", token);
			return token;
		} catch (Exception e) {
			log.error("RefreshToken 생성 실패: {}", e.getMessage());
			throw e;
		}
	}

	public String getEmailFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claims.getSubject();
	}

	public boolean validateToken(String token) {
		try {
			// Bearer 제거
			if (token.startsWith("Bearer ")) {
				token = token.substring(7);
			}

			Jwts.parserBuilder()
					.setSigningKey(getSigningKey())
					.build()
					.parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Authentication getAuthentication(String token) {
		String email = getEmailFromToken(token);
		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}
}
