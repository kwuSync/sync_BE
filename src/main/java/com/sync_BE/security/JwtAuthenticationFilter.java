package com.sync_BE.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sync_BE.service.TokenService.RedisTokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final RedisTokenBlacklistService redisTokenBlacklistService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		try {
			String jwt = resolveToken(request);
			log.debug("JWT Token: {}", jwt);

			if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
				boolean isBlacklisted = false;
				try {
					isBlacklisted = redisTokenBlacklistService.isBlacklisted(jwt);
				} catch (Exception redisEx) {
					log.error("Redis 연결 실패로 블랙리스트 검증을 건너뜀: {}", redisEx.getMessage());
				}

				if (isBlacklisted) {
					log.warn("블랙리스트에 등록된 토큰: {}...", jwt.substring(0, Math.min(10, jwt.length())));
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}

				Authentication authentication = jwtProvider.getAuthentication(jwt);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.debug("Set Authentication to security context for '{}', uri: {}",
					authentication.getName(), request.getRequestURI());
			}
		} catch (Exception e) {
			log.error("Cannot set user authentication: {}", e.getMessage(), e);
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/api/user/login")
			|| path.startsWith("/api/user/join")
			|| path.startsWith("/api/mail/")
			|| path.startsWith("/api/reissue");
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7).trim();
		}
		return null;
	}
}
