package com.sum_news_BE.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document("news_comment")
public class NewsComment {

	@Id
	private Integer id;

	private String comment_text;

	private LocalDateTime created_at;

	private LocalDateTime updated_at;
}
