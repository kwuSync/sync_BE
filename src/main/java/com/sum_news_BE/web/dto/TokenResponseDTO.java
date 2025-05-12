package com.sum_news_BE.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponseDTO {
	private String accessToken;
	private String refreshToken;

}
