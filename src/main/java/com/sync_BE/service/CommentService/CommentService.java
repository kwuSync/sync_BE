package com.sync_BE.service.CommentService;

import com.sync_BE.web.dto.commentDTO.CommentRequestDTO;
import com.sync_BE.web.dto.commentDTO.CommentResponseDTO;
import org.bson.types.ObjectId;

public interface CommentService {

    CommentResponseDTO.CommentActionDTO createComment(String clusterId, CommentRequestDTO.CreateDTO request, ObjectId userId);

    CommentResponseDTO.CommentDTO getCommentById(String commentId);

    CommentResponseDTO.CommentListDTO getCommentsByClusterId(String clusterId, ObjectId currentUserId);

    CommentResponseDTO.CommentActionDTO updateComment(String commentId, CommentRequestDTO.UpdateDTO request, ObjectId userId);

    CommentResponseDTO.CommentActionDTO deleteComment(String commentId, ObjectId userId);

    CommentResponseDTO.CommentCountDTO getCommentCountByClusterId(String clusterId);
}
