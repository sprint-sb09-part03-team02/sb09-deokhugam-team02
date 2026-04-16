package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

  Optional<Review> findByIdAndIsDeletedFalse(UUID id);

  // 1인 1리뷰 명세 준수: book.id와 user.id를 조회
  boolean existsByBookIdAndUserIdAndIsDeletedFalse(UUID bookId, UUID userId);
}