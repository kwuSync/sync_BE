package com.sum_news_BE.service.NewsService;

import com.sum_news_BE.web.dto.newsDTO.NewsResponseDTO;

public interface NewsService {
	// 메인 화면 뉴스 목록 조회
	NewsResponseDTO.NewsListDTO getMain();

	// 뉴스 요약 조회 (기사 ID)
	NewsResponseDTO.NewsClusterDTO getNewsSummary(String articleId);

	// 뉴스 요약 조회 (클러스터 ID)
	NewsResponseDTO.NewsClusterDTO getNewsSummaryByClusterId(String clusterId);

}
