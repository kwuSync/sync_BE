package com.sync_BE.api.exception;

public class RedisConnectionFailureException extends RuntimeException {
	public RedisConnectionFailureException(String message) {
		super(message);
	}

	public RedisConnectionFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
