package com.sum_news_BE.web.dto.newsDTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewsSummaryResponseDTO {
	private String clusterId;
	private String summary;
	private LocalDateTime generatedAt;
}
