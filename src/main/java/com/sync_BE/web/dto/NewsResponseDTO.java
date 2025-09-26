package com.sync_BE.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class NewsResponseDTO {

    @Getter
    @Setter
    @Builder
    public static class NewsArticleDTO {
        private String id;
        private String title;
        private String summaryText;
        private String source;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    public static class NewsListDTO {
        private List<NewsArticleDTO> newsList;
    }
}
