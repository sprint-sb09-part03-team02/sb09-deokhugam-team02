package com.deokhugam.deokhugam_server.domain.review.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewSearchRequest {

  private UUID userId;
  private UUID bookId;
  private String keyword;

  @NotNull(message = "요청자 ID는 필수입니다.")
  private UUID requestUserId;

  private String orderBy = "createdAt";
  private String direction = "DESC";

  private String cursor;
  private LocalDateTime after;

  private Integer limit = 50;
}