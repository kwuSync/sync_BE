package com.sync_BE.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sync_BE.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CorsConfig corsConfig;

	public static final String[] url = {
		"/",
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/swagger-resources/**",
		"/v3/api-docs/**",
		"/api-docs/**",
		"/api-docs/json/swagger-config",
		"/api-docs/json",
		"/user/join",
		"/user/login",
		"/user/password/**",
		"/mail/send",
		"/mail/verify",
		"/reissue"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.addFilter(corsConfig.corsFilter())
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(url).permitAll()
				.anyRequest().authenticated())
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
