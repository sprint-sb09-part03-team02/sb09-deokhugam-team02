package com.deokhugam.deokhugam_server.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * "Deokhugam-Request-User-ID" 헤더에서 UUID를 읽어 request attribute에 저장합니다.
 * 컨트롤러에서는 @RequestAttribute("requestUserId") UUID userId 로 꺼내 사용합니다.
 */
@Component
public class RequestUserIdInterceptor implements HandlerInterceptor {

  public static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";
  public static final String USER_ID_ATTRIBUTE = "requestUserId";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    String userIdHeader = request.getHeader(USER_ID_HEADER);
    if (userIdHeader != null && !userIdHeader.isBlank()) {
      try {
        UUID userId = UUID.fromString(userIdHeader);
        request.setAttribute(USER_ID_ATTRIBUTE, userId);
      } catch (IllegalArgumentException ignored) {
        // 유효하지 않은 UUID이면 attribute 미설정 (인증 필터에서 걸림)
      }
    }
    return true;
  }
}
