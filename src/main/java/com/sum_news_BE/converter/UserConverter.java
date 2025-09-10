package com.sum_news_BE.converter;

import java.time.LocalDateTime;

import com.sum_news_BE.domain.User;
import com.sum_news_BE.web.dto.userDTO.UserRequestDTO;

public class UserConverter {

	// 회원가입 시 dto -> entity
	public static User toUser(UserRequestDTO.JoinDTO joinDTO) {
		return User.builder()
			.nickname(joinDTO.getNickname())
			.email(joinDTO.getEmail())
			.password(joinDTO.getPassword())
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
	}

}
