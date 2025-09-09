package com.sum_news_BE.web.controller;


import com.sum_news_BE.api.ApiResponse;
import com.sum_news_BE.domain.NewsSummary;
import com.sum_news_BE.service.NewsService.NewsService;
import com.sum_news_BE.web.dto.NewsResponseDTO;
import com.sum_news_BE.web.dto.NewsSummaryResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@GetMapping("/{article_id}/summary")
	@Operation(summary = "뉴스요약 조회 API", description = "기사 요약을 조회합니다.")
	public ApiResponse<NewsSummaryResponseDTO> getNewsSummary(@PathVariable("article_id") String articleId) {
		NewsSummaryResponseDTO summary = newsService.getNewsSummary(articleId);
		return ApiResponse.ok("뉴스 요약 조회가 완료되었습니다.", summary);
	}
}
