package com.sum_news_BE.security;

import org.springframework.security.core.userdetails.UserDetails;
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
	public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
		User user = userRepository.findByUserid(userid)
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
		return new CustomUserDetails(user);
	}
}
