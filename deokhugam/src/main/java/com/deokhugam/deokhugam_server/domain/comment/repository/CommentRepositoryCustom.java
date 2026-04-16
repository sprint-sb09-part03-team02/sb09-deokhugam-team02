package com.deokhugam.deokhugam_server.domain.comment.repository;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {
  List<Comment> searchComments(CommentSearchRequest request);

  // 전체 개수 조회를 위한 메서드 추가
  long countComments(UUID reviewId);
}