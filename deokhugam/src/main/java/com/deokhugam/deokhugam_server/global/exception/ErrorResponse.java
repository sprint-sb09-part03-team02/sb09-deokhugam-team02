package com.deokhugam.deokhugam_server.global.exception;

public record ErrorResponse(
    int status,
    String code,
    String message
) {
  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(
        errorCode.getStatus(),
        errorCode.getCode(),
        errorCode.getMessage()
    );
  }
}