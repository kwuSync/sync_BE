package com.sync_BE.web.dto.commentDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class CommentRequestDTO {
    
    @Getter
    @Setter
    public static class CreateDTO {
        @NotBlank(message = "댓글 내용은 필수 입력값입니다.")
        @Size(min = 1, max = 1000, message = "댓글은 1~1000자까지 입력 가능합니다.")
        private String commentText;
    }
    
    @Getter
    @Setter
    public static class UpdateDTO {
        @NotBlank(message = "댓글 내용은 필수 입력값입니다.")
        @Size(min = 1, max = 1000, message = "댓글은 1~1000자까지 입력 가능합니다.")
        private String commentText;
    }
}
