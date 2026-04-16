package com.deokhugam.deokhugam_server.domain.user.repository;

import static com.deokhugam.deokhugam_server.domain.comment.entity.QComment.comment;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReviewLike.reviewLike;
import static com.deokhugam.deokhugam_server.domain.user.entity.QUser.user;

import com.deokhugam.deokhugam_server.domain.user.dto.response.UserRankQueryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public List<UserRankQueryDto> findUserActivityStatistics(LocalDate start, LocalDate end) {

    return queryFactory
        .select(Projections.constructor(UserRankQueryDto.class,
            user.id,
            review.countDistinct(),
            reviewLike.countDistinct(),
            comment.countDistinct()
        ))
        .from(user)
        .leftJoin(review).on(review.userId.eq(user.id))
        .leftJoin(reviewLike).on(reviewLike.userId.eq(user.id))
        .leftJoin(comment).on(comment.userId.eq(user.id))
        .where(user.createdAt.between(start.atStartOfDay(), end.atTime(LocalTime.MAX)))
        .groupBy(user.id)
        .fetch();
  }
}