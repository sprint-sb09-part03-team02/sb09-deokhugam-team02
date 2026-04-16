package com.deokhugam.deokhugam_server.domain.comment.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentSearchRequest {

  @NotNull(message = "리뷰 ID는 필수 조회 조건입니다.")
  private UUID reviewId;

  private UUID userId;

  private String orderBy = "createdAt";
  private String direction = "DESC";

  private String cursor;
  private LocalDateTime after;

  private Integer limit = 50;
}