package com.deokhugam.deokhugam_server.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.firewall.StrictHttpFirewall;

class SecurityConfigTest {

  @Test
  @DisplayName("제공 프론트 번들의 정적 이미지 경로 처리를 위해 이중 슬래시 차단을 완화한다")
  void httpFirewall_allowsDoubleSlashStaticResourcePath() {
    SecurityConfig securityConfig = new SecurityConfig();

    StrictHttpFirewall firewall = securityConfig.httpFirewall();

    assertThat(firewall.getDecodedUrlBlocklist()).doesNotContain("//");
  }
}
