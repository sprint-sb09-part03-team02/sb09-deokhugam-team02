package com.deokhugam.deokhugam_server.domain.review.dto.response;

public record ReviewRankQueryDto(
    Long reviewId,
    Long likeCount,
    Long commentCount
) {
  public Double calculateScore() {
    return (likeCount * 0.3) + (commentCount * 0.7);
  }
}
