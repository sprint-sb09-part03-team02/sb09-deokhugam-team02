package com.deokhugam.deokhugam_server.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateTimeUtilsTest {

  @Test
  @DisplayName("성공: null 또는 blank는 null을 반환한다")
  void parseLocalDateTime_NullOrBlank() {
    assertThat(DateTimeUtils.parseLocalDateTime(null)).isNull();
    assertThat(DateTimeUtils.parseLocalDateTime(" ")).isNull();
  }

  @Test
  @DisplayName("성공: ISO 로컬 날짜시간을 파싱한다")
  void parseLocalDateTime_IsoLocalDateTime() {
    assertThat(DateTimeUtils.parseLocalDateTime("2026-04-28T11:30:00"))
      .isEqualTo(LocalDateTime.of(2026, 4, 28, 11, 30));
  }

  @Test
  @DisplayName("성공: 공백 구분 날짜시간을 ISO 형식으로 보정해 파싱한다")
  void parseLocalDateTime_SpaceSeparatedDateTime() {
    assertThat(DateTimeUtils.parseLocalDateTime("2026-04-28 11:30:00"))
      .isEqualTo(LocalDateTime.of(2026, 4, 28, 11, 30));
  }

  @Test
  @DisplayName("성공: Zulu 시간은 KST LocalDateTime으로 변환한다")
  void parseLocalDateTime_ZonedUtc() {
    assertThat(DateTimeUtils.parseLocalDateTime("2026-04-28T02:30:00Z"))
      .isEqualTo(LocalDateTime.of(2026, 4, 28, 11, 30));
  }

  @Test
  @DisplayName("성공: 날짜만 유효하면 해당 날짜의 시작 시간으로 변환한다")
  void parseLocalDateTime_DateFallback() {
    assertThat(DateTimeUtils.parseLocalDateTime("2026-04-28-invalid"))
      .isEqualTo(LocalDateTime.of(2026, 4, 28, 0, 0));
  }

  @Test
  @DisplayName("실패: 파싱할 수 없는 값은 null을 반환한다")
  void parseLocalDateTime_Invalid() {
    assertThat(DateTimeUtils.parseLocalDateTime("invalid")).isNull();
  }
}
