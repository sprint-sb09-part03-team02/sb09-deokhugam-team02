package com.deokhugam.deokhugam_server.domain.book.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.api")
public record NaverApiProperties(
  String clientId,
  String clientSecret,
  String url
) {
}
