package com.deokhugam.deokhugam_server.domain.review.dto.response;

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
    int rank,
    double score,
    long likeCount,
    long commentCount
) {}