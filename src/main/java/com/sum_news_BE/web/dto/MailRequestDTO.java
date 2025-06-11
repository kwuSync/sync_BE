package com.sum_news_BE.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class MailRequestDTO {

	@Getter
	@Setter
	public static class SendDTO {
		private String email;
	}

	@Getter
	@Setter
	public static class VerifyDTO {
		private String email;
		private int authNumber;
	}
}
