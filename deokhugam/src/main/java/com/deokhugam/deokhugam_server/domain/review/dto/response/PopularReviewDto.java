package com.deokhugam.deokhugam_server.domain.review.dto.response;

import com.deokhugam.deokhugam_server.domain.user.dto.response.Period;
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
    Period period,
    LocalDateTime createdAt,
    int rank,
    double score,
    long likeCount,
    long commentCount
) {}