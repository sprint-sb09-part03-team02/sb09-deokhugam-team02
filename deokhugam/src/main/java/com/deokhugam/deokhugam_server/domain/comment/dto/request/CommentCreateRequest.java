package com.deokhugam.deokhugam_server.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentCreateRequest(
    @NotNull(message = "리뷰 ID는 필수입니다.")
    UUID reviewId,

    @NotNull(message = "사용자 ID는 필수입니다.")
    UUID userId,

    @NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
    String content
) {}