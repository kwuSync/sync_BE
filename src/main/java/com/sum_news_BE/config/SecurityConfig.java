package com.sum_news_BE.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	public static final String[] url = {
		"/",
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/swagger-resources/**",
		"/v3/api-docs/**",
		"/api-docs/**",
		"/api-docs/json/swagger-config",
		"/api-docs/json",
		"/user/signup",
		"/user/login",
		"/reissue"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(request -> request
				.requestMatchers(url).permitAll()
				.anyRequest().authenticated())
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(HttpBasicConfigurer::disable);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
