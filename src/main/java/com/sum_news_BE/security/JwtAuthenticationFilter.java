package com.sum_news_BE.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final UserDetailsService userDetailsService;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.equals("/reissue") || path.equals("/user/login") || path.equals("/user/signup");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			String accessToken = request.getHeader("Authorization");
			if (accessToken == null || accessToken.isEmpty()) {
				filterChain.doFilter(request, response);
				return;
			}

			// Bearer 제거
			if (accessToken.startsWith("Bearer ")) {
				accessToken = accessToken.substring(7);
			}

			// 토큰 유효성 검증
			if (!jwtProvider.validateToken(accessToken)) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			// 토큰에서 userid 가져온다
			String userid = jwtProvider.getUseridFromToken(accessToken);

			// userdetail 객체
			UserDetails userDetails = userDetailsService.loadUserByUsername(userid);

			// userdetail 객체를 SecurityContextHolder에
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken
				(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

			filterChain.doFilter(request, response);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
