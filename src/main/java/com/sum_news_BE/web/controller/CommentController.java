package com.sum_news_BE.web.controller;

import com.sum_news_BE.api.ApiResponse;
import com.sum_news_BE.security.CustomUserDetails;
import com.sum_news_BE.service.CommentService.CommentService;
import com.sum_news_BE.web.dto.commentDTO.CommentRequestDTO;
import com.sum_news_BE.web.dto.commentDTO.CommentResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cluster/{clusterId}/comment")
@Tag(name = "comment", description = "댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 작성", description = "댓글을 작성합니다.")
    public ApiResponse<CommentResponseDTO.CommentActionDTO> createComment(
            @PathVariable String clusterId,
            @Valid @RequestBody CommentRequestDTO.CreateDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ObjectId userId = userDetails.getUser().getId();
        
        CommentResponseDTO.CommentActionDTO result = commentService.createComment(clusterId, request, userId);
        return ApiResponse.ok(result.getMessage(), result);
    }

    @GetMapping
    @Operation(summary = "댓글 목록 조회", description = "댓글 목록을 조회합니다.")
    public ApiResponse<CommentResponseDTO.CommentListDTO> getComments(
            @PathVariable String clusterId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ObjectId userId = userDetails.getUser().getId();
        
        CommentResponseDTO.CommentListDTO result = commentService.getCommentsByClusterId(clusterId, userId);
        return ApiResponse.ok("댓글 목록 조회가 완료되었습니다.", result);
    }

    @GetMapping("/count")
    @Operation(summary = "댓글 수 조회", description = "댓글 수를 조회합니다.")
    public ApiResponse<CommentResponseDTO.CommentCountDTO> getCommentCount(
            @PathVariable String clusterId) {

        CommentResponseDTO.CommentCountDTO result = commentService.getCommentCountByClusterId(clusterId);
        return ApiResponse.ok("댓글 수 조회가 완료되었습니다.", result);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    public ApiResponse<CommentResponseDTO.CommentActionDTO> updateComment(
            @PathVariable String clusterId,
            @PathVariable String commentId,
            @Valid @RequestBody CommentRequestDTO.UpdateDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ObjectId userId = userDetails.getUser().getId();
        
        CommentResponseDTO.CommentActionDTO result = commentService.updateComment(commentId, request, userId);
        return ApiResponse.ok(result.getMessage(), result);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    public ApiResponse<CommentResponseDTO.CommentActionDTO> deleteComment(
            @PathVariable String clusterId,
            @PathVariable String commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ObjectId userId = userDetails.getUser().getId();
        
        CommentResponseDTO.CommentActionDTO result = commentService.deleteComment(commentId, userId);
        return ApiResponse.ok(result.getMessage(), result);
    }
}
