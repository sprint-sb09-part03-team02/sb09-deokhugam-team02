package com.deokhugam.deokhugam_server.domain.user.repository;

import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.entity.QPowerUser;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PowerUserRepositoryImpl implements PowerUserRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public List<PowerUser> findPowerUsersDynamic(Period period, Integer cursor, LocalDateTime after, String direction, int limit, LocalDate latestDate) {
    QPowerUser powerUser = QPowerUser.powerUser;

    return queryFactory
      .selectFrom(powerUser)
      .join(powerUser.user).fetchJoin()
      .where(
        powerUser.periodType.eq(period),
        powerUser.calculatedDate.eq(latestDate),
        cursorCondition(powerUser, cursor, direction)
      )
      .orderBy(direction.equalsIgnoreCase("ASC") ?
          powerUser.rankOrder.asc() : powerUser.rankOrder.desc()
      )
      .limit(limit + 1)
      .fetch();
  }

  private BooleanExpression cursorCondition(QPowerUser pu, Integer cursor, String direction) {
    if ( cursor == null)  return null;

    if (direction.equalsIgnoreCase("DESC")) {
      return pu.rankOrder.gt(cursor);
    } else {
      return pu.rankOrder.lt(cursor);
    }
  }
}
