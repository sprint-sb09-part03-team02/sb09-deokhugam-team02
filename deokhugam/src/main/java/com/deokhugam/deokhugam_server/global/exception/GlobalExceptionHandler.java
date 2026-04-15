package com.deokhugam.deokhugam_server.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 1. 비즈니스 로직 예외 처리
  @ExceptionHandler(DeokhugamException.class)
  protected ResponseEntity<ErrorResponse> handleDeokhugamException(DeokhugamException e) {
    log.error("DeokhugamException: {}", e.getErrorCode().getMessage());

    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse response = ErrorResponse.of(errorCode);

    return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus()));
  }

  // 2. Bean Validation 예외 처리 (명세서 400 대응)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.error("MethodArgumentNotValidException: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // 3. 그 외 예상치 못한 시스템 예외 (명세서 500 대응)
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Unhandled Exception: ", e);

    ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}