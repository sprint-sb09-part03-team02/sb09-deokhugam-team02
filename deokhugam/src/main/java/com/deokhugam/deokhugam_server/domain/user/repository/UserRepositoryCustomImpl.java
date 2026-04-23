package com.deokhugam.deokhugam_server.domain.user.repository;

import static com.deokhugam.deokhugam_server.domain.comment.entity.QComment.comment;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReviewLike.reviewLike;
import static com.deokhugam.deokhugam_server.domain.user.entity.QUser.user;

import com.deokhugam.deokhugam_server.domain.user.dto.response.UserRankQueryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<UserRankQueryDto> findUserActivityStatistics(LocalDate start, LocalDate end) {
    LocalDateTime startDt = start.atStartOfDay();
    LocalDateTime endDt = end.atTime(LocalTime.MAX);

    return queryFactory
      .select(Projections.constructor(UserRankQueryDto.class,
        user.id,
        review.id.countDistinct(),
        reviewLike.id.countDistinct(),
        comment.id.countDistinct()
      ))
      .from(user)
      .leftJoin(review).on(
        review.user.id.eq(user.id),
        review.createdAt.between(startDt, endDt)
      )
      .leftJoin(reviewLike).on(
        reviewLike.user.id.eq(user.id),
        reviewLike.createdAt.between(startDt, endDt)
      )
      .leftJoin(comment).on(
        comment.userId.eq(user.id),
        comment.createdAt.between(startDt, endDt)
      )
      .where(user.isDeleted.isFalse())
      .groupBy(user.id)
      .fetch();
  }
}
