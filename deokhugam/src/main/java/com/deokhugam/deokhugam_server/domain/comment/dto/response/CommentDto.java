package com.deokhugam.deokhugam_server.domain.comment.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommentDto(
    UUID id,
    UUID reviewId,
    UUID userId,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}