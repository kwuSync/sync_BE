package com.sum_news_BE.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.Builder;

@Data
@NoArgsConstructor
@Document(collection = "news_summary")
public class NewsSummary {

	@Id
	private ObjectId id;

	private String articleId;

	private String summaryText;

	private LocalDateTime generatedAt;

	@DBRef
	private NewsArticle article;

}
