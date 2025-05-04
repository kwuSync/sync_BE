package com.sum_news_BE.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document("news_summary")
public class NewsSummary {

	@Id
	private Integer id;

	private Integer article_id;

	private String summary_text;

	private LocalDateTime generated_at;
}
