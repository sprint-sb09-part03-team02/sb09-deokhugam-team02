package com.deokhugam.deokhugam_server.global.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class StaticImagePathMapper {

  private static final String IMAGES_PREFIX = "images/";

  @Named("normalizeStaticImagePath")
  public String normalizeStaticImagePath(String thumbnailUrl) {
    if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
      return thumbnailUrl;
    }

    String normalized = thumbnailUrl.trim();
    if (normalized.contains("://") || normalized.startsWith("//")) {
      return normalized;
    }

    while (normalized.startsWith("/")) {
      normalized = normalized.substring(1);
    }

    if (normalized.startsWith(IMAGES_PREFIX)) {
      return normalized.substring(IMAGES_PREFIX.length());
    }

    return normalized;
  }
}
