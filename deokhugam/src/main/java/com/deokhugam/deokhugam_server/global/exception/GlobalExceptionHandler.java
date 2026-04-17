package com.deokhugam.deokhugam_server.global.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

  // 4. HTTP 메소드 불일치 (405) / 리소스 없음 (404)
  @ExceptionHandler(NoResourceFoundException.class)
  protected ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
    log.warn("NoResourceFoundException: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);

    return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
  }

  // 5. @Validated 파라미터 검증 실패 (400)
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
    log.warn("ConstraintViolationException: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // 6. 필수 헤더 누락 (400)
  @ExceptionHandler(MissingRequestHeaderException.class)
  protected ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException e) {
    log.warn("MissingRequestHeaderException: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // 7. 요청 Body 파싱 실패 (400)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
    log.warn("HttpMessageNotReadableException: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}