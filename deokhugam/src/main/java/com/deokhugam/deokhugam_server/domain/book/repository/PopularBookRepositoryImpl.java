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
        cursorCondition(popularBook, cursor, direction)
      )
      .orderBy(direction.equalsIgnoreCase("ASC") ?
        popularBook.rankOrder.asc() : popularBook.rankOrder.desc()
      )
      .limit(limit + 1)
      .fetch();
  }

  private BooleanExpression cursorCondition(QPopularBook pb, Integer cursor, String direction) {
    if (cursor == null) return null;

    if (direction.equalsIgnoreCase("ASC")) {
      return pb.rankOrder.gt(cursor);
    } else {
      return pb.rankOrder.lt(cursor);
    }
  }
}
