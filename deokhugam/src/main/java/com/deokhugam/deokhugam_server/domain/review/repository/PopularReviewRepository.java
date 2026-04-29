package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserScoreDto;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID>, PopularReviewRepositoryCustom {

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

  long countByPeriodType(Period periodType);

  @Query("SELECT MAX(pr.calculatedDate) FROM PopularReview pr WHERE pr.periodType = :periodType")
  Optional<LocalDate> findMaxCalculatedDateByPeriodType(@Param("periodType") Period periodType);

}
