package com.sync_BE.web.dto;

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
