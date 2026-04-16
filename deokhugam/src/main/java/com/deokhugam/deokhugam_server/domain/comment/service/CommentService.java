package com.deokhugam.deokhugam_server.domain.comment.service;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import java.util.UUID;

public interface CommentService {
  // 댓글 등록
  CommentDto createComment(CommentCreateRequest request);

  // 댓글 수정
  CommentDto updateComment(UUID commentId, UUID requestUserId, CommentUpdateRequest request);

  // 댓글 삭제 (논리 삭제)
  void deleteComment(UUID commentId, UUID requestUserId);

  // 댓글 상세 조회
  CommentDto getComment(UUID commentId);

  // 리뷰별 댓글 목록 조회
  CursorPageResponse<CommentDto> getCommentsByReviewId(CommentSearchRequest request);
}