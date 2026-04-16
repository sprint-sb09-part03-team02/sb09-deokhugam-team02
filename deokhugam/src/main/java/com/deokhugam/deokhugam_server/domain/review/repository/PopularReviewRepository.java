package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {
  @Query("select sum(pr.score) from PopularReview pr " +
      "join pr.review r " + // Review 엔티티와 조인
      "where r.userId = :userId " +
      "and pr.periodType = :period " +
      "and pr.calculatedDate = :date")
  Optional<Double> sumScoreByUserIdAndPeriod(
      @Param("userId") UUID userId,
      @Param("period") Period period,
      @Param("date") LocalDate date
  );

  List<PopularReview> findAllByPeriodTypeAndCalculatedDate(Period periodType, LocalDate calculatedDate);

}
