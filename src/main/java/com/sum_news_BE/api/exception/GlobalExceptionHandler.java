package com.sum_news_BE.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sum_news_BE.api.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("BadCredentialsException: ", e);
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("401", "아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: ", e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("400", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: ", e);
        
        String message = e.getMessage();
        String[] parts = message.split(":", 2);
        
        if (parts.length == 2) {
            String code = parts[0];
            String detailMessage = parts[1];
            
            switch (code) {
                case "COMMENT_NOT_FOUND":
                case "ARTICLE_NOT_FOUND":
                case "CLUSTER_NOT_FOUND":
                case "USER_NOT_FOUND":
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("404", detailMessage));
                        
                case "COMMENT_ACCESS_DENIED":
                case "ARTICLE_ACCESS_DENIED":
                case "CLUSTER_ACCESS_DENIED":
                case "USER_ACCESS_DENIED":
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("403", detailMessage));
                        
                case "INVALID_COMMENT":
                case "INVALID_ARTICLE":
                case "INVALID_CLUSTER":
                case "INVALID_USER":
                case "INVALID_TOKEN":
                case "MISSING_HEADER":
                case "INVALID_FORMAT":
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("400", detailMessage));
                        
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("500", detailMessage));
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("500", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Exception: ", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("500", "서버 내부 오류가 발생했습니다."));
    }
}