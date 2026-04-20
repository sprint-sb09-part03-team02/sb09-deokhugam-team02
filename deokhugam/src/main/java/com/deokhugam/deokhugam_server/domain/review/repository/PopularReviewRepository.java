package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

  @Query("select sum(pr.score) from PopularReview pr " +
      "join pr.review r " +
      "where r.user.id = :userId " +
      "and pr.periodType = :period " +
      "and pr.calculatedDate = :date")
  Optional<Double> sumScoreByUserIdAndPeriod(
      @Param("userId") UUID userId,
      @Param("period") Period period,
      @Param("date") LocalDate date
  );

  List<PopularReview> findAllByPeriodTypeAndCalculatedDate(Period periodType,
      LocalDate calculatedDate);

  @Query("select p from PopularReview p " +
      "join fetch p.review r " +
      "WHERE p.periodType = :period " +
      "AND (" +
      "  :after IS NULL OR " +
      "  (:direction = 'DESC' AND (p.createdAt < :after OR (p.createdAt = :after AND p.rankOrder > :cursor))) OR "
      +
      "  (:direction = 'ASC' AND (p.createdAt > :after OR (p.createdAt = :after AND p.rankOrder < :cursor)))"
      +
      ") " +
      "ORDER BY " +
      "  CASE WHEN :direction = 'DESC' THEN p.createdAt END DESC, " +
      "  CASE WHEN :direction = 'DESC' THEN p.rankOrder END ASC, " +
      "  CASE WHEN :direction = 'ASC' THEN p.createdAt END ASC, " +
      "  CASE WHEN :direction = 'ASC' THEN p.rankOrder END DESC")
  List<PopularReview> findPopularReviewsWithPaging(
      @Param("period") Period period,
      @Param("direction") String direction,
      @Param("cursor") Integer cursor,
      @Param("after") LocalDateTime after,
      @Param("limit") Limit limit
  );

  long countByPeriodType(Period periodType);


}
