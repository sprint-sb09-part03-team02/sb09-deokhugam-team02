package com.deokhugam.deokhugam_server.global.util;

import com.deokhugam.deokhugam_server.global.type.Period;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PeriodUtil {

  private static final LocalDateTime ALL_TIME_START = LocalDateTime.of(2020, 1, 1, 0, 0);
  private static final LocalDate ALL_TIME_START_DATE = LocalDate.of(2000, 1, 1);

  private PeriodUtil() {}

  /** Period → LocalDateTime 변환 (조회용) */
  public static LocalDateTime calculateStartTime(Period period) {
    return switch (period) {
      case DAILY    -> LocalDateTime.now().minusDays(1);
      case WEEKLY   -> LocalDateTime.now().minusWeeks(1);
      case MONTHLY  -> LocalDateTime.now().minusMonths(1);
      case ALL_TIME -> ALL_TIME_START;
    };
  }

  /** Period → LocalDate 변환 (배치 랭킹 계산용) */
  public static LocalDate getStartDate(Period period, LocalDate endDate) {
    return switch (period) {
      case DAILY    -> endDate;
      case WEEKLY   -> endDate.minusWeeks(1);
      case MONTHLY  -> endDate.minusMonths(1);
      case ALL_TIME -> ALL_TIME_START_DATE;
    };
  }
}

