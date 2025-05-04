package com.sum_news_BE.web.dto;

import com.sum_news_BE.domain.NewsComment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NewsArticleDetailResponseDTO { //뉴스 상세페이지 DTO
    private Integer id;

    private String title;

    private String content;

    private String source;

    private LocalDateTime publishedAt;

    private List<NewsComment> comments;
}
