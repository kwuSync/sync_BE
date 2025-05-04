package com.sum_news_BE.domain;

import java.time.LocalDateTime;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.Builder;


@Data
@Builder
@Document(collection = "news_summary")
public class NewsSummary {

	@Id
	private Integer id;

	private String summary_text;

	private LocalDateTime generated_at;

	@DBRef
	private NewsArticle article;
}
