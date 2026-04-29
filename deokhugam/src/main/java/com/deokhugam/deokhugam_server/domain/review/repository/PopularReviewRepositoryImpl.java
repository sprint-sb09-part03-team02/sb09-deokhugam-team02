package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.QPopularReview;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopularReviewRepositoryImpl implements PopularReviewRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public List<PopularReview> findPopularReviewDynamic(Period period, Integer cursor,
    LocalDateTime after, String direction, int limit, LocalDate latestDate) {
    QPopularReview popularReview = QPopularReview.popularReview;

    return queryFactory
      .selectFrom(popularReview)
      .join(popularReview.review).fetchJoin()
      .where(
        popularReview.periodType.eq(period),
        popularReview.calculatedDate.eq(latestDate),
        cursorCondition(popularReview, cursor, after, direction)
      )
      .orderBy(direction.equalsIgnoreCase("DESC") ?
          popularReview.createdAt.desc() : popularReview.createdAt.asc(),
        popularReview.rankOrder.desc())
      .limit(limit + 1)
      .fetch();
  }

  private BooleanExpression cursorCondition(QPopularReview pu, Integer cursor, LocalDateTime after, String direction) {
    if (after == null || cursor == null) {
      return null;
    }

    if (direction.equalsIgnoreCase("DESC")) {
      return pu.createdAt.lt(after)
        .or(pu.createdAt.eq(after).and(pu.rankOrder.gt(cursor)));
    } else {
      return pu.createdAt.gt(after)
        .or(pu.createdAt.eq(after).and(pu.rankOrder.lt(cursor)));
    }
  }
}
