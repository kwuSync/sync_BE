package com.sum_news_BE.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document("news_article")
public class NewsArticle {

	@Id
	private Integer id;

	private String title;

	private String content;

	private LocalDateTime published_at;

	private LocalDateTime created_at;

	private String source;


}
