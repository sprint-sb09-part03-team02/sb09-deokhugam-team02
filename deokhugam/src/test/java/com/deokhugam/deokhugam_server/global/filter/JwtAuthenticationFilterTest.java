package com.deokhugam.deokhugam_server.global.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

  private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter();

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("성공: 유효한 사용자 ID 헤더가 있으면 SecurityContext 인증 정보를 설정한다")
  void doFilter_SetsAuthentication() throws ServletException, IOException {
    UUID userId = UUID.randomUUID();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(USER_ID_HEADER, userId.toString());
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<Authentication> authenticationInChain = new AtomicReference<>();
    FilterChain chain = (req, res) ->
      authenticationInChain.set(SecurityContextHolder.getContext().getAuthentication());

    filter.doFilter(request, response, chain);

    assertThat(authenticationInChain.get()).isNotNull();
    assertThat(authenticationInChain.get().getPrincipal()).isEqualTo(userId);
  }

  @Test
  @DisplayName("성공: 사용자 ID 헤더가 없으면 인증 정보를 설정하지 않는다")
  void doFilter_NoHeader() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("성공: 잘못된 UUID 헤더는 인증 없이 통과한다")
  void doFilter_InvalidHeader() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(USER_ID_HEADER, "invalid-uuid");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
