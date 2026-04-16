package com.deokhugam.deokhugam_server.domain.comment.service;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import java.util.UUID;

public interface CommentService {
  CommentDto createComment(CommentCreateRequest request);
  CommentDto updateComment(UUID commentId, UUID requestUserId, CommentUpdateRequest request);
  void deleteComment(UUID commentId, UUID requestUserId); // 논리 삭제

  void permanentDeleteComment(UUID commentId, UUID requestUserId); // 물리 삭제

  CommentDto getComment(UUID commentId);
  CursorPageResponse<CommentDto> getCommentsByReviewId(CommentSearchRequest request);
}