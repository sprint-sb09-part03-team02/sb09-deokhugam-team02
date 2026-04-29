package com.deokhugam.deokhugam_server.domain.review.service;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewLikeDto;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ReviewService {
  ReviewDto createReview(ReviewCreateRequest request);

  ReviewDto getReview(UUID reviewId, UUID requestUserId);

  CursorPageResponse<ReviewDto> searchReviews(ReviewSearchRequest request);

  ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId);

  void deleteReview(UUID reviewId, UUID requestUserId);

  void hardDeleteReview(UUID reviewId, UUID requestUserId);

  ReviewLikeDto likeReview(UUID reviewId, UUID requestUserId);

  CursorPageResponse<PopularReviewDto> searchPopularReviews(Period period, String direction, String cursor, LocalDateTime after, int limit);
}
