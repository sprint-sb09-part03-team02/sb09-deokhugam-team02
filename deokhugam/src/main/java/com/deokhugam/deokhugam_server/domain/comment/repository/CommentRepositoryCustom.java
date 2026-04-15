package com.deokhugam.deokhugam_server.domain.comment.repository;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import java.util.List;

public interface CommentRepositoryCustom {
  List<Comment> searchComments(CommentSearchRequest request);
}