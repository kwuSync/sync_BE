package com.sum_news_BE.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "userNewsLog")
public class UserNewsLog {

    @Id
    private Integer id;

    @DBRef
    private User user;

    @DBRef
    private NewsArticle article;

    private boolean is_read;

    private LocalDateTime read_at;
}
