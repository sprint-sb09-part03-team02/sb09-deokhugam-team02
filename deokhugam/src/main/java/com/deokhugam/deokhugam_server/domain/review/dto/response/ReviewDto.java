package com.deokhugam.deokhugam_server.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewDto(
    UUID id,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String content,
    int rating,
    int likeCount,
    int commentCount,
    boolean likedByMe,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}