package com.sum_news_BE.api;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sum_news_BE.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ApiResponse<T> {

	private final String code;
	private final String message;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T data;

	//성공시
	public static <T> ApiResponse<T> ok(T data, User user) {
		return new ApiResponse<>("200", "성공", data);
	}

	public static <T> ApiResponse<T> error(HttpStatus status, String message) {
		return new ApiResponse<>(String.valueOf(status.value()), message, null);
	}

	public static <T> ApiResponse<T> error(String code, String message) {
		return new ApiResponse<>(code, message, null);
	}

}
