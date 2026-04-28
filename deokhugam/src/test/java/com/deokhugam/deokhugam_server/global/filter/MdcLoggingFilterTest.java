package com.deokhugam.deokhugam_server.global.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class MdcLoggingFilterTest {

  private final MdcLoggingFilter filter = new MdcLoggingFilter();

  @Test
  @DisplayName("성공: 요청 ID와 X-Forwarded-For의 첫 번째 IP를 MDC와 응답 헤더에 설정한다")
  void doFilter_SetsMdcAndResponseHeader() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> requestIdInChain = new AtomicReference<>();
    AtomicReference<String> clientIpInChain = new AtomicReference<>();

    FilterChain chain = (req, res) -> {
      requestIdInChain.set(MDC.get("requestId"));
      clientIpInChain.set(MDC.get("clientIp"));
    };

    filter.doFilter(request, response, chain);

    assertThat(requestIdInChain.get()).hasSize(12);
    assertThat(clientIpInChain.get()).isEqualTo("203.0.113.10");
    assertThat(response.getHeader("X-Request-Id")).isEqualTo(requestIdInChain.get());
    assertThat(MDC.get("requestId")).isNull();
    assertThat(MDC.get("clientIp")).isNull();
  }

  @Test
  @DisplayName("성공: X-Forwarded-For가 없으면 remoteAddr를 clientIp로 사용한다")
  void doFilter_UsesRemoteAddr() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("198.51.100.20");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> clientIpInChain = new AtomicReference<>();

    FilterChain chain = (req, res) -> clientIpInChain.set(MDC.get("clientIp"));

    filter.doFilter(request, response, chain);

    assertThat(clientIpInChain.get()).isEqualTo("198.51.100.20");
  }
}
