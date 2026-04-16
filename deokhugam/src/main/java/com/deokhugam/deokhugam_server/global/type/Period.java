package com.deokhugam.deokhugam_server.global.type;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Period {
  DAILY, WEEKLY, MONTHLY, ALL_TIME;

  @JsonCreator
  public static Period from(String value) {
    return Period.valueOf(value.toUpperCase());
  }
}
