package com.deokhugam.deokhugam_server.domain.user.repository;

import static com.deokhugam.deokhugam_server.domain.comment.entity.QComment.comment;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReviewLike.reviewLike;
import static com.deokhugam.deokhugam_server.domain.user.entity.QUser.user;

import com.deokhugam.deokhugam_server.domain.user.dto.response.UserRankQueryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
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

    var reviewsCount = JPAExpressions
      .select(review.id.count())
      .from(review)
      .where(review.user.id.eq(user.id).and(review.createdAt.between(startDt, endDt)));

    var likesCount = JPAExpressions
      .select(reviewLike.id.count())
      .from(reviewLike)
      .where(reviewLike.user.id.eq(user.id).and(reviewLike.createdAt.between(startDt, endDt)));

    var commentsCount = JPAExpressions
      .select(comment.id.count())
      .from(comment)
      .where(comment.userId.eq(user.id).and(comment.createdAt.between(startDt, endDt)));

    return queryFactory
      .select(Projections.constructor(UserRankQueryDto.class,
        user.id,
        reviewsCount,
        likesCount,
        commentsCount
      ))
      .from(user)
      .where(user.isDeleted.isFalse(),
        reviewsCount.gt(0L).or(likesCount.gt(0L)).or(commentsCount.gt(0L))
        )
      .fetch();
  }
}
