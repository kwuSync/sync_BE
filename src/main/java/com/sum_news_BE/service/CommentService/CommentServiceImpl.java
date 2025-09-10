package com.sum_news_BE.service.CommentService;

import com.sum_news_BE.api.exception.AuthorizationException;
import com.sum_news_BE.api.exception.ResourceNotFoundException;
import com.sum_news_BE.domain.Comment;
import com.sum_news_BE.domain.User;
import com.sum_news_BE.repository.NewsArticleRepository;
import com.sum_news_BE.repository.CommentRepository;
import com.sum_news_BE.repository.NewsSummaryRepository;
import com.sum_news_BE.repository.UserRepository;
import com.sum_news_BE.web.dto.commentDTO.CommentRequestDTO;
import com.sum_news_BE.web.dto.commentDTO.CommentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public CommentResponseDTO.CommentActionDTO createComment(String clusterId, CommentRequestDTO.CreateDTO request, ObjectId userId) {
        User user = findUserById(userId);

        Comment comment = Comment.builder()
                .clusterId(clusterId)
                .user(user)
                .commentText(request.getCommentText())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return buildActionResponse(savedComment.getId().toHexString(), "댓글이 작성되었습니다.", "CREATE");
    }

    @Override
    public CommentResponseDTO.CommentDTO getCommentById(String commentId) {
        Comment comment = findCommentById(commentId);
        return convertToCommentDTO(comment, null);
    }

    @Override
    public CommentResponseDTO.CommentListDTO getCommentsByClusterId(String clusterId, ObjectId currentUserId) {
        List<Comment> comments = commentRepository.findByClusterIdOrderByCreatedAtDesc(clusterId);
        List<CommentResponseDTO.CommentDTO> commentDTOs = comments.stream()
                .map(comment -> convertToCommentDTO(comment, currentUserId))
                .collect(Collectors.toList());
        return CommentResponseDTO.CommentListDTO.builder()
                .comments(commentDTOs)
                .totalCount((long) commentDTOs.size())
                .build();
    }

    @Override
    public CommentResponseDTO.CommentActionDTO updateComment(String commentId, CommentRequestDTO.UpdateDTO request, ObjectId userId) {
        Comment comment = findCommentById(commentId);
        validateCommentOwnership(comment, userId);
        comment.setCommentText(request.getCommentText());
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
        return buildActionResponse(commentId, "댓글이 수정되었습니다.", "UPDATE");
    }

    @Override
    public CommentResponseDTO.CommentActionDTO deleteComment(String commentId, ObjectId userId) {
        Comment comment = findCommentById(commentId);
        validateCommentOwnership(comment, userId);
        commentRepository.delete(comment);
        return buildActionResponse(commentId, "댓글이 삭제되었습니다.", "DELETE");
    }

    @Override
    public CommentResponseDTO.CommentCountDTO getCommentCountByClusterId(String clusterId) {
        long count = commentRepository.countByClusterId(clusterId);
        return CommentResponseDTO.CommentCountDTO.builder()
                .commentCount(count)
                .build();
    }

    private User findUserById(ObjectId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private Comment findCommentById(String commentId) {
        return commentRepository.findById(new ObjectId(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));
    }

    private void validateCommentOwnership(Comment comment, ObjectId userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new AuthorizationException("댓글을 수정/삭제할 권한이 없습니다.");
        }
    }

    private CommentResponseDTO.CommentActionDTO buildActionResponse(String commentId, String message, String actionType) {
        return CommentResponseDTO.CommentActionDTO.builder()
                .commentId(commentId)
                .message(message)
                .actionType(actionType)
                .build();
    }

    private CommentResponseDTO.CommentDTO convertToCommentDTO(Comment comment, ObjectId currentUserId) {
        boolean isOwner = currentUserId != null && comment.getUser().getId().equals(currentUserId);
        return CommentResponseDTO.CommentDTO.builder()
                .commentId(comment.getId().toHexString())
                .userId(comment.getUser().getId().toHexString())
                .userName(comment.getUser().getNickname())
                .commentText(comment.getCommentText())
                .clusterId(comment.getClusterId().toString())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isOwner(isOwner)
                .build();
    }
}
