package com.sum_news_BE.domain;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Document(collection = "refresh_token")
public class RefreshToken {

	@Id
	private ObjectId id;

	private String refreshToken;

	private String userid;

	public RefreshToken(String userid, String refreshToken) {
		this.userid = userid;
		this.refreshToken = refreshToken;
	}
}
