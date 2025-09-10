package com.sum_news_BE.web.controller;


import com.sum_news_BE.api.ApiResponse;
import com.sum_news_BE.service.NewsService.NewsService;
import com.sum_news_BE.web.dto.newsDTO.NewsResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main")
@Tag(name = "news", description = "뉴스 관련 API")
public class NewsController {

	private final NewsService newsService;

	@GetMapping("/news")
	@Operation(summary = "뉴스목록 API", description = "메인 화면에서 뉴스 목록을 조회합니다.")
	public ApiResponse<NewsResponseDTO.NewsListDTO> getMain() {
		NewsResponseDTO.NewsListDTO newsList = newsService.getMain();
		return ApiResponse.ok("목록 조회가 완료되었습니다.", newsList);
	}

	@GetMapping("/cluster/{cluster_id}/summary")
	@Operation(summary = "뉴스요약 조회 API", description = "클러스터 ID로 요약을 조회합니다.")
	public ApiResponse<NewsResponseDTO.NewsClusterDTO> getNewsSummaryByClusterId(@PathVariable("cluster_id") String clusterId) {
		NewsResponseDTO.NewsClusterDTO newsCluster = newsService.getNewsSummaryByClusterId(clusterId);
		return ApiResponse.ok("뉴스 요약 조회가 완료되었습니다.", newsCluster);
	}
}
