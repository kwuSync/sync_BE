package com.sum_news_BE.domain;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;

@Data
@Builder
@Document(collection = "news_article")
public class NewsArticle {

	@Id
	private Integer id;

	private String title;

	private String content;

	private LocalDateTime published_at;

	private LocalDateTime created_at;

	private String source;

	@DBRef
	private List<NewsComment> comments;

}
