package com.deokhugam.deokhugam_server.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PeriodUtilTest {

  @Test
  @DisplayName("성공: 배치 기준 시작 날짜를 기간별로 계산한다")
  void getStartDate_ByPeriod() {
    LocalDate endDate = LocalDate.of(2026, 4, 28);

    assertThat(PeriodUtil.getStartDate(Period.DAILY, endDate)).isEqualTo(endDate);
    assertThat(PeriodUtil.getStartDate(Period.WEEKLY, endDate)).isEqualTo(LocalDate.of(2026, 4, 21));
    assertThat(PeriodUtil.getStartDate(Period.MONTHLY, endDate)).isEqualTo(LocalDate.of(2026, 3, 28));
    assertThat(PeriodUtil.getStartDate(Period.ALL_TIME, endDate)).isEqualTo(LocalDate.of(2000, 1, 1));
  }

  @Test
  @DisplayName("성공: 조회 기준 시작 시간을 기간별로 계산한다")
  void calculateStartTime_ByPeriod() {
    LocalDateTime before = LocalDateTime.now();

    assertThat(PeriodUtil.calculateStartTime(Period.DAILY)).isBeforeOrEqualTo(before);
    assertThat(PeriodUtil.calculateStartTime(Period.WEEKLY)).isBeforeOrEqualTo(before.minusDays(6));
    assertThat(PeriodUtil.calculateStartTime(Period.MONTHLY)).isBeforeOrEqualTo(before.minusDays(27));
    assertThat(PeriodUtil.calculateStartTime(Period.ALL_TIME)).isEqualTo(LocalDateTime.of(2020, 1, 1, 0, 0));
  }
}
