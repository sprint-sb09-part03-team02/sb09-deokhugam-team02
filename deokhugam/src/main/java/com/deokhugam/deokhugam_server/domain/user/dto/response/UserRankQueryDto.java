package com.deokhugam.deokhugam_server.domain.user.dto.response;

public record UserRankQueryDto(
    Long userId,
    Double totalReviewScore,
    Long givenLikeCount,
    Long writtenCommentCount
) {
  public Double calculateScore() {
    return (totalReviewScore * 0.5) + (givenLikeCount * 0.2) + (writtenCommentCount * 0.3);
  }
}