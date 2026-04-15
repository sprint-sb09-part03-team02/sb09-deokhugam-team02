package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import java.util.List;

public interface ReviewRepositoryCustom {
  List<Review> searchReviews(ReviewSearchRequest request);
}