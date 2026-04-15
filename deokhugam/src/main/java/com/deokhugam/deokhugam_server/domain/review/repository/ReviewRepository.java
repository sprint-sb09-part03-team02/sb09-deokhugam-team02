package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

  Optional<Review> findByIdAndIsDeletedFalse(UUID id);
  boolean existsByBookIdAndUserIdAndIsDeletedFalse(UUID bookId, UUID userId);
}