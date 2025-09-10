package com.sum_news_BE.web.dto.newsDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class NewsResponseDTO {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsClusterDTO {
        private String generatedTitle;
        private List<String> generatedKeywords;
        private String clusterId;
        private Summary summary;
        private List<String> titles;
        private List<Integer> ids;
        private LocalDateTime timestamp;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private String highlight;
        private String article;
        private String background;
    }

    @Getter
    @Setter
    @Builder
    public static class NewsArticleDTO {
        private String id;
        private String title;
        private String summaryText;
        private String clusterId;
        private LocalDateTime timestamp;
        private String source;
        private LocalDateTime publishedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsListDTO {
        private List<NewsArticleDTO> newsList;
    }
}
