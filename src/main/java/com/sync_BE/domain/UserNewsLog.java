package com.sync_BE.domain;

import java.time.LocalDateTime;

import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Setter
@Document(collection = "user_news_log")
public class UserNewsLog {

    @Id
    private ObjectId id;

    @DBRef
    private User user;

    @DBRef
    private NewsArticle article;

    private boolean isRead;

    private LocalDateTime readAt;
}
