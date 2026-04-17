package com.deokhugam.deokhugam_server.domain.review.service;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.List;
import java.util.UUID;

public interface ReviewService {
  // 리뷰 등록
  ReviewDto createReview(ReviewCreateRequest request);

  // 리뷰 상세 조회
  ReviewDto getReview(UUID reviewId, UUID requestUserId);

  // 리뷰 목록 조회 (검색 및 페이지네이션)
  CursorPageResponse<ReviewDto> searchReviews(ReviewSearchRequest request);

  // 리뷰 수정
  ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId);

  // 리뷰 삭제 (논리 삭제)
  void deleteReview(UUID reviewId, UUID requestUserId);

  //인기 리뷰 목록 조회
  List<ReviewDto> searchPopularReviews(Period period, String direction, String cursor, String after, int limit);

}