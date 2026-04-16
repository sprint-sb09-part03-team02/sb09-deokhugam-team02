package com.deokhugam.deokhugam_server.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

  private final boolean success;
  private final int status; // 상태 코드 숫자 (예: 201, 204) 추가
  private final T data;
  private final String message;

  // 성공 응답 (데이터 + 상태 코드)
  public static <T> ApiResponse<T> success(T data, HttpStatus status) {
    return new ApiResponse<>(true, status.value(), data, status.getReasonPhrase());
  }

  // 성공 응답 (데이터만 - 기본 200 OK)
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, 200, data, "Success");
  }

  // 실패 응답
  public static <T> ApiResponse<T> error(String message, HttpStatus status) {
    return new ApiResponse<>(false, status.value(), null, message);
  }
}