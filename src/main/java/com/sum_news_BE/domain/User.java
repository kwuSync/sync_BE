package com.sum_news_BE.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	private ObjectId id;

	private String nickname;

	private String password;

	private String email;

	private Role role;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@DBRef
	private UserSetting userSetting;

	public void updateNickname(String nickname) {
		this.nickname = nickname;
		this.updatedAt = LocalDateTime.now();
	}

	public void updatePassword(String password) {
		this.password = password;
		this.updatedAt = LocalDateTime.now();
	}
}
