package com.deokhugam.deokhugam_server.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 요청 헤더 "Deokhugam-Request-User-ID"에서 UUID를 읽어 SecurityContext에 인증 정보를 설정합니다.
 * 유효한 UUID가 존재하면 인증된 사용자로 처리합니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    String userIdHeader = request.getHeader(USER_ID_HEADER);
    if (userIdHeader != null && !userIdHeader.isBlank()) {
      try {
        UUID userId = UUID.fromString(userIdHeader);
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (IllegalArgumentException ignored) {
        // 유효하지 않은 UUID 형식이면 인증 설정 없이 통과
      }
    }

    chain.doFilter(request, response);
  }
}
