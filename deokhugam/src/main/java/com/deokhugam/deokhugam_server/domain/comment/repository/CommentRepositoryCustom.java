package com.deokhugam.deokhugam_server.domain.comment.repository;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {
  List<CommentDto> searchComments(CommentSearchRequest request);

  long countComments(UUID reviewId);
}