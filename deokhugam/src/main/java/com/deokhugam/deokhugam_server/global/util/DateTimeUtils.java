package com.deokhugam.deokhugam_server.global.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {
  private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

  public static LocalDateTime parseLocalDateTime(String after) {
    if (after == null || after.isBlank()) {
      return null;
    }
    String trimmed = after.trim();

    try {
      if (trimmed.endsWith("Z")) {
        return LocalDateTime.ofInstant(Instant.parse(trimmed), KST_ZONE);
      }
      if (trimmed.contains(" ") && trimmed.length() >= 19) {
        trimmed = trimmed.replace(" ", "T");
      }
      return LocalDateTime.parse(trimmed);

    } catch (DateTimeParseException e) {
      try {
        if (trimmed.length() >= 10) {
          return LocalDate.parse(trimmed.substring(0, 10)).atStartOfDay();
        }
      } catch (Exception ignored) {
      }
      return null;
    }
  }
}
