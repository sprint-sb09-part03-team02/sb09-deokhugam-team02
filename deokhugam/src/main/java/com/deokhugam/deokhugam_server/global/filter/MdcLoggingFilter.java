package com.deokhugam.deokhugam_server.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 요청마다 MDC에 requestId(UUID)와 clientIp를 설정합니다.
 * 로그 패턴의 %X{requestId}, %X{clientIp} 변수와 매핑됩니다.
 */
public class MdcLoggingFilter extends OncePerRequestFilter {

  private static final String REQUEST_ID = "requestId";
  private static final String CLIENT_IP = "clientIp";

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain chain) throws ServletException, IOException {

    String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    String clientIp = resolveClientIp(request);

    MDC.put(REQUEST_ID, requestId);
    MDC.put(CLIENT_IP, clientIp);

    // 응답 헤더에 requestId 노출 (디버깅 편의)
    response.setHeader("X-Request-Id", requestId);

    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(REQUEST_ID);
      MDC.remove(CLIENT_IP);
    }
  }

  private String resolveClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip != null && !ip.isBlank()) {
      // X-Forwarded-For: client, proxy1, proxy2 → 첫 번째가 실제 클라이언트
      return ip.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
