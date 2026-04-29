package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.entity.QPopularBook;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopularBookRepositoryImpl implements PopularBookRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public List<PopularBook> findPopularBooksDynamic(Period period, Integer cursor, LocalDateTime after, String direction, int limit, LocalDate latestDate) {
    QPopularBook popularBook = QPopularBook.popularBook;

    return queryFactory
      .selectFrom(popularBook)
      .join(popularBook.book).fetchJoin()
      .where(
        popularBook.periodType.eq(period),
        popularBook.calculatedDate.eq(latestDate),
        cursorCondition(popularBook, cursor, after, direction)
      )
      .orderBy(direction.equalsIgnoreCase("DESC") ?
          popularBook.createdAt.desc() : popularBook.createdAt.asc(),
        popularBook.rankOrder.desc())
      .limit(limit + 1)
      .fetch();
  }

  private BooleanExpression cursorCondition(QPopularBook pb, Integer cursor, LocalDateTime after, String direction) {
    if (after == null || cursor == null) return null;

    if (direction.equalsIgnoreCase("DESC")) {
      return pb.createdAt.lt(after)
        .or(pb.createdAt.eq(after).and(pb.rankOrder.gt(cursor)));
    } else {
      return pb.createdAt.gt(after)
        .or(pb.createdAt.eq(after).and(pb.rankOrder.lt(cursor)));
    }
  }
}
