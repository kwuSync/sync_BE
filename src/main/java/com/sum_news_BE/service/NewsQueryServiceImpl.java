package com.sum_news_BE.service;

import com.sum_news_BE.domain.NewsArticle;
import com.sum_news_BE.domain.NewsSummary;
import com.sum_news_BE.repository.NewsArticleRepository;
import com.sum_news_BE.repository.NewsCommentRepository;
import com.sum_news_BE.repository.NewsSummaryRepository;
import com.sum_news_BE.web.dto.NewsArticleDetailResponseDTO;
import com.sum_news_BE.web.dto.NewsArticleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NewsQueryServiceImpl implements NewsQueryService {

    private final NewsArticleRepository newsArticleRepository;
    private final NewsSummaryRepository newsSummaryRepository;
    private final NewsCommentRepository newsCommentRepository;

    @Override
    public List<NewsArticleResponseDTO> getNewsList() {
        List<NewsArticle> articles = newsArticleRepository.findAll();
        return articles.stream().map(article -> {
            NewsSummary newsSummary = (NewsSummary) newsSummaryRepository.findByArticleId(article.getId());
            NewsArticleResponseDTO dto = new NewsArticleResponseDTO();
            dto.setId(article.getId());
            dto.setTitle(article.getTitle());
            dto.setSource(article.getSource());
            dto.setPublishedAt(article.getPublished_at());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public NewsArticleDetailResponseDTO getNewsDetail(Integer articleId) {
        return null;
    }
}
