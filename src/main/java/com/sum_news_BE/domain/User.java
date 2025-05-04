package com.sum_news_BE.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Document("user")
@Getter
@Builder
public class User{

	@Id
	private Integer id;

	private String userid;

	private String password;

	private String name;

	private LocalDateTime created_at;

	private LocalDateTime updated_at;

}
