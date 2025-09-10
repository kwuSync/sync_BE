package com.sum_news_BE.web.dto.commentDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponseDTO {
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 정보")
    public static class CommentDTO {
        @Schema(description = "댓글 ID")
        private String commentId;
        
        @Schema(description = "사용자 ID")
        private String userId;
        
        @Schema(description = "사용자 닉네임")
        private String userName;
        
        @Schema(description = "댓글 내용")
        private String commentText;
        
        @Schema(description = "클러스터 ID")
        @JsonProperty("cluster_id")
        private String clusterId;
        
        @Schema(description = "작성일시")
        private LocalDateTime createdAt;
        
        @Schema(description = "수정일시")
        private LocalDateTime updatedAt;
        
        @Schema(description = "댓글 작성자 여부")
        private Boolean isOwner;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 목록 응답")
    public static class CommentListDTO {
        @Schema(description = "댓글 목록")
        private List<CommentDTO> comments;
        
        @Schema(description = "전체 댓글 수")
        private Long totalCount;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 수 응답")
    public static class CommentCountDTO {
        @Schema(description = "댓글 수")
        private Long commentCount;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 작성/수정/삭제 성공 응답")
    public static class CommentActionDTO {
        @Schema(description = "댓글 ID")
        private String commentId;
        
        @Schema(description = "성공 메시지")
        private String message;
        
        @Schema(description = "작업 타입")
        private String actionType;
    }
}
