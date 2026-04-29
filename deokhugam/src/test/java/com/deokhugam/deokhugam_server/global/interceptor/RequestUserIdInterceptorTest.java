package com.deokhugam.deokhugam_server.global.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestUserIdInterceptorTest {

  private final RequestUserIdInterceptor interceptor = new RequestUserIdInterceptor();

  @Test
  @DisplayName("성공: 유효한 사용자 ID 헤더를 request attribute에 UUID로 저장한다")
  void preHandle_ValidUserIdHeader() {
    UUID userId = UUID.randomUUID();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(RequestUserIdInterceptor.USER_ID_HEADER, userId.toString());

    boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

    assertThat(result).isTrue();
    assertThat(request.getAttribute(RequestUserIdInterceptor.USER_ID_ATTRIBUTE)).isEqualTo(userId);
  }

  @Test
  @DisplayName("성공: 헤더가 없으면 attribute를 설정하지 않고 통과한다")
  void preHandle_NoHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

    assertThat(result).isTrue();
    assertThat(request.getAttribute(RequestUserIdInterceptor.USER_ID_ATTRIBUTE)).isNull();
  }

  @Test
  @DisplayName("성공: 잘못된 UUID 헤더는 attribute를 설정하지 않고 통과한다")
  void preHandle_InvalidHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(RequestUserIdInterceptor.USER_ID_HEADER, "invalid-uuid");

    boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

    assertThat(result).isTrue();
    assertThat(request.getAttribute(RequestUserIdInterceptor.USER_ID_ATTRIBUTE)).isNull();
  }
}
