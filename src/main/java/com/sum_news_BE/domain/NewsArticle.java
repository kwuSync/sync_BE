package com.sum_news_BE.domain;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;

@Data
@NoArgsConstructor
@Document(collection = "news_article")
public class NewsArticle {

	@Id
	private ObjectId id;

	private String title;

	private String content;

	private LocalDateTime publishedAt;

	private LocalDateTime createdAt;

	private String source;

	@DBRef
	private List<NewsComment> comments;
}
