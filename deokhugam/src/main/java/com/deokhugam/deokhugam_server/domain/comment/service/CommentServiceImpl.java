package com.deokhugam.deokhugam_server.domain.comment.service;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import com.deokhugam.deokhugam_server.domain.comment.event.CommentCreatedEvent;
import com.deokhugam.deokhugam_server.domain.comment.mapper.CommentMapper;
import com.deokhugam.deokhugam_server.domain.comment.repository.CommentRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.util.CursorPageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final CommentMapper commentMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public CommentDto createComment(CommentCreateRequest request) {
    // 1. 리뷰 존재 확인
    reviewRepository.findById(request.reviewId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

    // 2. 작성자 존재 확인
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND));

    // 3. 엔티티 생성 및 저장
    Comment comment = Comment.builder()
        .reviewId(request.reviewId())
        .userId(request.userId())
        .content(request.content())
        .build();
    Comment savedComment = commentRepository.save(comment);

    // 4. 알림 생성을 위한 이벤트 발행
    eventPublisher.publishEvent(new CommentCreatedEvent(
        savedComment.getReviewId(),
        savedComment.getId(),
        savedComment.getUserId()
    ));

    return commentMapper.toDto(savedComment, user.getNickname());
  }

  @Override
  @Transactional
  public CommentDto updateComment(UUID commentId, UUID requestUserId, CommentUpdateRequest request) {
    Comment comment = findCommentOrThrow(commentId);

    // 본인 작성 확인
    if (!comment.getUserId().equals(requestUserId)) {
      throw new DeokhugamException(ErrorCode.NOT_COMMENT_OWNER);
    }

    comment.updateContent(request.content());
    User user = findUserOrThrow(comment.getUserId());

    return commentMapper.toDto(comment, user.getNickname());
  }

  @Override
  @Transactional
  public void deleteComment(UUID commentId, UUID requestUserId) {
    Comment comment = findCommentOrThrow(commentId);

    // 본인 확인
    if (!comment.getUserId().equals(requestUserId)) {
      throw new DeokhugamException(ErrorCode.NOT_COMMENT_OWNER);
    }

    // 논리 삭제 원칙 준수
    comment.delete();
  }

  @Override
  public CommentDto getComment(UUID commentId) {
    Comment comment = findCommentOrThrow(commentId);
    User user = findUserOrThrow(comment.getUserId());
    return commentMapper.toDto(comment, user.getNickname());
  }

  @Override
  public CursorPageResponse<CommentDto> getCommentsByReviewId(CommentSearchRequest request) {
    // QueryDSL을 통한 커서 기반 조회 (limit + 1)
    List<Comment> items = commentRepository.searchComments(request);
    long totalElements = commentRepository.countComments(request.getReviewId());

    return CursorPageUtil.toResponse(
        items,
        request.getLimit(),
        totalElements,
        comment -> {
          User user = findUserOrThrow(comment.getUserId());
          return commentMapper.toDto(comment, user.getNickname());
        },
        comment -> comment.getId().toString(),
        Comment::getCreatedAt
    );
  }

  private Comment findCommentOrThrow(UUID commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.COMMENT_NOT_FOUND));
  }

  private User findUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND));
  }
}