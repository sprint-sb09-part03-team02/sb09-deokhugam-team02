package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import java.time.LocalDate;
import java.util.List;

public interface ReviewRepositoryCustom {

  List<ReviewDto> searchReviews(ReviewSearchRequest request);

  List<ReviewRankQueryDto> findReviewStatistics(LocalDate start, LocalDate end);
}