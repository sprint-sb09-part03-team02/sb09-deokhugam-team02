package com.deokhugam.deokhugam_server.global.type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PeriodTest {

  @Test
  @DisplayName("성공: 대소문자와 무관하게 기간 값을 변환한다")
  void from_ValidValue() {
    assertThat(Period.from("daily")).isEqualTo(Period.DAILY);
    assertThat(Period.from("WEEKLY")).isEqualTo(Period.WEEKLY);
    assertThat(Period.from("monthly")).isEqualTo(Period.MONTHLY);
    assertThat(Period.from("all_time")).isEqualTo(Period.ALL_TIME);
  }

  @Test
  @DisplayName("실패: null 또는 blank 기간 값이면 예외를 던진다")
  void from_NullOrBlank_ThrowsException() {
    assertInvalidPeriod(null);
    assertInvalidPeriod(" ");
  }

  @Test
  @DisplayName("실패: 지원하지 않는 기간 값이면 예외를 던진다")
  void from_InvalidValue_ThrowsException() {
    assertInvalidPeriod("yearly");
  }

  private void assertInvalidPeriod(String value) {
    assertThatThrownBy(() -> Period.from(value))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_PERIOD);
  }
}
