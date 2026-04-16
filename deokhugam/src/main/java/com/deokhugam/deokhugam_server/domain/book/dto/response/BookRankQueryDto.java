package com.deokhugam.deokhugam_server.domain.book.dto.response;

import java.util.UUID;

public record BookRankQueryDto(
    UUID bookId,
    Long reviewCount,
    Double avgRating
) {
  public Double calculateScore() {
    return (reviewCount * 0.4) + (avgRating * 0.6);
  }
}