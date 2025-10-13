package com.sync_BE.web.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewsSummaryResponseDTO {
	private String articleId;
	private String summaryText;
	private LocalDateTime generatedAt;
}
