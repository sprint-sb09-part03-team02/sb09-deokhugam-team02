package com.deokhugam.deokhugam_server.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtProviderTest {

  private JwtProvider jwtProvider;

  @BeforeEach
  void setUp() {
    jwtProvider = new JwtProvider();
    String secret = Base64.getEncoder()
      .encodeToString("12345678901234567890123456789012".getBytes());
    ReflectionTestUtils.setField(jwtProvider, "secret", secret);
    ReflectionTestUtils.setField(jwtProvider, "expiration", 60_000L);
  }

  @Test
  @DisplayName("성공: 사용자 ID로 JWT를 생성하고 다시 사용자 ID를 추출한다")
  void generateTokenAndGetUserId() {
    UUID userId = UUID.randomUUID();

    String token = jwtProvider.generateToken(userId);

    assertThat(jwtProvider.validateToken(token)).isTrue();
    assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(userId);
  }

  @Test
  @DisplayName("실패: 잘못된 JWT는 검증에 실패한다")
  void validateToken_InvalidToken() {
    assertThat(jwtProvider.validateToken("invalid.token.value")).isFalse();
  }

  @Test
  @DisplayName("실패: 만료된 JWT는 검증에 실패한다")
  void validateToken_ExpiredToken() {
    ReflectionTestUtils.setField(jwtProvider, "expiration", -1L);

    String token = jwtProvider.generateToken(UUID.randomUUID());

    assertThat(jwtProvider.validateToken(token)).isFalse();
  }
}
