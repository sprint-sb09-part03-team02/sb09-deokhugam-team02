package com.deokhugam.deokhugam_server.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PopularReviewDto(
    UUID id,
    UUID reviewId,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String reviewContent,
    double reviewRating,
    String period,
    LocalDateTime createdAt, // 명세서 요구사항에 따라 추가
    int rank,
    double score,
    long likeCount,
    long commentCount
) {}