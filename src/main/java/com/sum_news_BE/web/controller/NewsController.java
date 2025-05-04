package com.sum_news_BE.web.controller;

import com.sum_news_BE.service.NewsQueryService;
import com.sum_news_BE.web.dto.NewsArticleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {
    
    private final NewsQueryService newsQueryService;
    
    @GetMapping()
    public List<NewsArticleResponseDTO> getNewsList() {
        return newsQueryService.getNewsList();
    }
}
