package com.deokhugam.deokhugam_server.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.firewall.StrictHttpFirewall;

class SecurityConfigTest {

  @Test
  @DisplayName("제공 프론트 번들의 정적 이미지 경로 처리를 위해 이중 슬래시 차단을 완화한다")
  void httpFirewall_allowsDoubleSlashStaticResourcePath() {
    SecurityConfig securityConfig = new SecurityConfig();

    StrictHttpFirewall firewall = securityConfig.httpFirewall();

    assertThat(firewall.getEncodedUrlBlocklist()).doesNotContain("//");
    assertThat(firewall.getDecodedUrlBlocklist()).doesNotContain("//");
  }

  @Test
  @DisplayName("이중 슬래시가 포함된 정적 이미지 요청은 Spring Security 방화벽에서 거부하지 않는다")
  void httpFirewall_doesNotRejectDoubleSlashStaticResourcePath() {
    SecurityConfig securityConfig = new SecurityConfig();
    StrictHttpFirewall firewall = securityConfig.httpFirewall();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/images//nav/deokhugam.svg");
    request.setServletPath("/images//nav/deokhugam.svg");

    assertThatNoException().isThrownBy(() -> firewall.getFirewalledRequest(request));
  }
}
