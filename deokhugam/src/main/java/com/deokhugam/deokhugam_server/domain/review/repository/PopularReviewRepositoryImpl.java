package com.deokhugam.deokhugam_server.domain.review.repository;

import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.QPopularReview;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.querydsl.core.types.dsl.BooleanExpression;
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
        cursorCondition(popularReview, cursor, direction)
      )
      .orderBy(popularReview.rankOrder.asc())
      .limit(limit + 1)
      .fetch();
  }

  private BooleanExpression cursorCondition(QPopularReview pr, Integer cursor, String direction) {
    if (cursor == null)
      return null;
    return pr.rankOrder.gt(cursor);
  }
}
