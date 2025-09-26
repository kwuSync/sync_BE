package com.sync_BE.security;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.sync_BE.repository.RefreshTokenRepository;
import com.sync_BE.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

	private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

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

		UserDetails userDetails = userDetailsService.loadUserByUsername(email);

		String authorities = userDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		return Jwts.builder()
			.setSubject(email)
			.claim("auth", authorities)
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
		log.info("Attempting to parse token: '[{}]'", token);

		Claims claims = Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token)
			.getBody();
		return claims.getSubject(); // subject에서 이메일 추출
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
