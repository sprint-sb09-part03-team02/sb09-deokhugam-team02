package com.deokhugam.deokhugam_server.domain.book.dto.response;

public record BookRankQueryDto(
    Long bookId,
    Long reviewCount,
    Double avgRating
) {
  public Double calculateScore() {
    return (reviewCount * 0.4) + (avgRating * 0.6);
  }
}