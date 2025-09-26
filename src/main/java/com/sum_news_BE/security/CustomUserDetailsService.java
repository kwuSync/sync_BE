package com.sum_news_BE.security;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sum_news_BE.domain.User;
import com.sum_news_BE.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
		return new CustomUserDetails(user);
	}

	private UserDetails createUserDetails(User user) {
		List<GrantedAuthority> authorities = Collections.singletonList(
			new SimpleGrantedAuthority(user.getRole().name())
		);

		return new org.springframework.security.core.userdetails.User(
			user.getEmail(),
			user.getPassword(),
			authorities
		);
	}
}
