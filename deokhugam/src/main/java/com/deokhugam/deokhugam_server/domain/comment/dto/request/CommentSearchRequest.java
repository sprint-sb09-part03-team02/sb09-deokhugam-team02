package com.deokhugam.deokhugam_server.domain.comment.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentSearchRequest {

  private UUID reviewId;
  private UUID userId;

  private String orderBy = "createdAt";
  private String direction = "DESC";

  private String cursor;
  private LocalDateTime after;

  private Integer limit = 50;
}