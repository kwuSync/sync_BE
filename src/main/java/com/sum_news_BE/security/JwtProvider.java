package com.sum_news_BE.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sum_news_BE.repository.RefreshTokenRepository;
import com.sum_news_BE.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

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

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes());
	}

	public String generateAccessToken(String userid) { //accessToken
		System.out.println("AccessToken 생성: " + userid);
		return Jwts.builder()
			.setSubject(userid)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public String generateRefreshToken(String userid) { //refreshToken
		System.out.println("RefreshToken 생성 시도: " + userid);
		try {
			String token = Jwts.builder()
				.setSubject(userid)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
			System.out.println("RefreshToken 생성 성공: " + token);
			return token;
		} catch (Exception e) {
			System.out.println("RefreshToken 생성 실패: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public String getUseridFromToken(String token) {
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
}
