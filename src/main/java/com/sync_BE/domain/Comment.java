package com.sync_BE.domain;

import java.time.LocalDateTime;

import lombok.Data;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "news_comment")
public class Comment {

	@Id
	private ObjectId id;

	private String clusterId;

	@DBRef
	private User user;

	private String commentText;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;
}
