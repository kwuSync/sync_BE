package com.sum_news_BE.service;

import com.sum_news_BE.web.dto.NewsArticleDetailResponseDTO;
import com.sum_news_BE.web.dto.NewsArticleResponseDTO;

import java.util.List;

public interface NewsQueryService {

    List<NewsArticleResponseDTO> getNewsList();

    NewsArticleDetailResponseDTO getNewsDetail(Integer articleId);
}
