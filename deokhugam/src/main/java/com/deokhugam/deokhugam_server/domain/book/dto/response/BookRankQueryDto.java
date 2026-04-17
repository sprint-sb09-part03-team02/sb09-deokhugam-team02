package com.deokhugam.deokhugam_server.domain.book.dto.response;

import java.util.UUID;

public record BookRankQueryDto(
        UUID bookId,
        Long reviewCount,
        Double avgRating
) {
  public Double calculateScore() {
    long safeReviewCount = reviewCount == null ? 0L : reviewCount;
    double safeAvgRating = avgRating == null ? 0.0 : avgRating;

    return (safeReviewCount * 0.4) + (safeAvgRating * 0.6);
  }
}