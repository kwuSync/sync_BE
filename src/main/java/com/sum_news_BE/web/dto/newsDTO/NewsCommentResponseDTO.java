package com.sum_news_BE.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsCommentResponseDTO {  // 뉴스댓글 ResponseDTO
    private Integer commentId;

    private String userName;

    private String commentText;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
