package com.sum_news_BE.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsResponseDTO {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsClusterDTO {
        private Integer clusterId;

        private String summary;

        private List<String> titles;

        private List<Integer> ids;

        private LocalDateTime timestamp;
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
