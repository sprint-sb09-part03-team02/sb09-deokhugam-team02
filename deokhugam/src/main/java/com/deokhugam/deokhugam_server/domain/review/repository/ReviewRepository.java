package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

  Optional<Review> findByIdAndIsDeletedFalse(UUID id);

  // 1인 1리뷰 명세 준수: book.id와 user.id를 조회
  boolean existsByBookIdAndUserIdAndIsDeletedFalse(UUID bookId, UUID userId);

  @Query("SELECT r FROM Review r WHERE r.createdAt >= :startTime AND r.isDeleted = false")
  List<PopularReview> findPopularReviewsWithPaging(
      @Param("startTime") LocalDateTime startTime,
      @Param("direction") String direction,
      @Param("cursor") String cursor,
      @Param("after") String after,
      @Param("limit") int limit
  );
}