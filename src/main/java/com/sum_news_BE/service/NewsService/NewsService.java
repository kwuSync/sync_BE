package com.sum_news_BE.service.NewsService;

import com.sum_news_BE.web.dto.NewsResponseDTO;
import com.sum_news_BE.web.dto.NewsSummaryResponseDTO;

import java.util.List;

public interface NewsService {
	// 메인 화면 뉴스 목록 조회
	NewsResponseDTO.NewsListDTO getMain();

	// 뉴스 요약 조회
	NewsSummaryResponseDTO getNewsSummary(String articleId);

}
