package com.deokhugam.deokhugam_server.global.config;

import com.deokhugam.deokhugam_server.global.filter.JwtAuthenticationFilter;
import com.deokhugam.deokhugam_server.global.filter.MdcLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(auth -> auth
            // ECS/ALB health check endpoint
            .requestMatchers("/actuator/health").permitAll()

            // 정적 프론트엔드와 SPA 화면 라우트는 브라우저 접근을 허용
            .requestMatchers(
                "/",
                "/index.html",
                "/api.json",
                "/assets/**",
                "/images/**",
                "/favicon.ico",
                "/login",
                "/signup",
                "/books/**",
                "/reviews/**"
            ).permitAll()

            // 1. 회원가입, 로그인만 허용 (비인증 접근 가능)
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users/login").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/books/popular").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reviews/popular").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/power").permitAll()

            // 2. 스웨거(Swagger) 관련 경로들 - 문자열로 간단하게!
            .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**"
            ).permitAll()

            // 3. 나머지는 인증 필요
            .anyRequest().authenticated()
        )
        .addFilterBefore(new MdcLoggingFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public StrictHttpFirewall httpFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.getDecodedUrlBlocklist().remove("//");
    return firewall;
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer(StrictHttpFirewall httpFirewall) {
    return web -> web.httpFirewall(httpFirewall);
  }

  // 임시 비밀번호 생성을 막기 위한 설정
  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
