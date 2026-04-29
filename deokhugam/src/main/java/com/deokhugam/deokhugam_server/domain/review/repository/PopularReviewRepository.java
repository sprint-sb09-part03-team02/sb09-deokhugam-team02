package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserScoreDto;
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

  @Query("SELECT new com.deokhugam.deokhugam_server.domain.user.dto.response.UserScoreDto(pr.review.user.id, SUM(pr.score)) " +
    "FROM PopularReview pr " +
    "WHERE pr.periodType = :periodType AND pr.calculatedDate = :date " +
    "GROUP BY pr.review.user.id")
  List<UserScoreDto> sumAllUserScoresByPeriod(@Param("periodType") Period periodType, @Param("date") LocalDate date);

  List<PopularReview> findAllByPeriodTypeAndCalculatedDate(Period periodType,
      LocalDate calculatedDate);

  @Query("SELECT r FROM PopularReview r " +
    "JOIN FETCH r.review rev " +
    "WHERE r.periodType = :period " +
    "AND (:after IS NULL OR r.createdAt < :after OR (r.createdAt = :after AND r.rankOrder > :cursor)) " +
    "ORDER BY r.createdAt DESC, r.rankOrder DESC")
  List<PopularReview> findPopularReviewsDesc(
    @Param("period") Period period,
    @Param("cursor") Integer cursor,
    @Param("after") LocalDateTime after,
    Limit limit
  );

  @Query("SELECT r FROM PopularReview r " +
    "JOIN FETCH r.review rev " +
    "WHERE r.periodType = :period " +
    "AND (:after IS NULL OR r.createdAt > :after OR (r.createdAt = :after AND r.rankOrder < :cursor)) " +
    "ORDER BY r.createdAt ASC, r.rankOrder ASC")
  List<PopularReview> findPopularReviewsAsc(
    @Param("period") Period period,
    @Param("cursor") Integer cursor,
    @Param("after") LocalDateTime after,
    Limit limit
  );

  long countByPeriodType(Period periodType);

  @Query("SELECT p FROM PopularReview p JOIN FETCH p.review b " +
    "WHERE p.periodType = :period " +
    "ORDER BY p.createdAt ASC, p.rankOrder ASC")
  List<PopularReview> findPopularReviewsAscFirstPage(@Param("period") Period period, Limit limit);

  @Query("SELECT p FROM PopularReview p JOIN FETCH p.review b " +
    "WHERE p.periodType = :period " +
    "ORDER BY p.createdAt DESC, p.rankOrder DESC")
  List<PopularReview> findPopularReviewsDescFirstPage(@Param("period") Period period, Limit limit);
}
