package com.deokhugam.deokhugam_server.domain.book.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ocr-space.api")
public record OcrSpaceProperties(
  String key,
  String url
) {
}
