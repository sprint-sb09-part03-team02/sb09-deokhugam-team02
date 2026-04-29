package com.deokhugam.deokhugam_server.global.type;

import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Period {
  DAILY, WEEKLY, MONTHLY, ALL_TIME;

  @JsonCreator
  public static Period from(String value) {
    if (value == null || value.isBlank()) {
      throw new DeokhugamException(ErrorCode.INVALID_PERIOD);
    }
    try {
      return Period.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeokhugamException(ErrorCode.INVALID_PERIOD);
    }
  }
}
