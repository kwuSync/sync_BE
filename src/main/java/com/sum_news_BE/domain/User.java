package com.sum_news_BE.domain;

import java.time.LocalDateTime;

import lombok.Data;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.Builder;


@Document(collection = "user")
@Data
@Builder
public class User{

	@Id
	private ObjectId id;

	private String userid;

	private String password;

	private String name;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@DBRef
	private UserSetting userSetting;

}
