package com.sum_news_BE.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
        private String generated_title;
        private List<String> generated_keywords;
        private Integer cluster_id;
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
        private String summary;
        private String clusterId;
        private LocalDateTime timestamp;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsListDTO {
        private List<NewsClusterDTO> newsClusters;
    }
}
