package com.sync_BE.converter;

import java.time.LocalDateTime;

import com.sync_BE.domain.User;
import com.sync_BE.web.dto.userDTO.UserRequestDTO;

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
