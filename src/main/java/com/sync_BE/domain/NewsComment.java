package com.sync_BE.domain;

import java.time.LocalDateTime;

import lombok.Data;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;

@Data
@Builder
@Document(collection = "news_comment")
public class NewsComment {

	@Id
	private ObjectId id;

	@DBRef
	private NewsArticle article;

	@DBRef
	private User user;

	private String comment_text;

	private LocalDateTime created_at;

	private LocalDateTime updated_at;
}
