package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

  // review.id와 user.id를 타고 들어가서 조회함
  Optional<ReviewLike> findByReviewIdAndUserId(UUID reviewId, UUID userId);

  boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);
}