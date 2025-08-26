package com.sum_news_BE.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "news_summary")
public class NewsSummary {

	@Id
	private ObjectId id;

	private String summaryText;

	private LocalDateTime generatedAt;

	@DBRef
	private NewsArticle article;

}
