package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {
  List<PopularReview> findAllByPeriodTypeAndCalculatedDate(Period type, LocalDate date);
}
