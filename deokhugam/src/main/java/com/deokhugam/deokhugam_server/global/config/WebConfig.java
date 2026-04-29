package com.deokhugam.deokhugam_server.global.config;

import com.deokhugam.deokhugam_server.global.interceptor.RequestUserIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final RequestUserIdInterceptor requestUserIdInterceptor;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOriginPatterns("*")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("Deokhugam-Request-User-ID", "X-Request-Id")
        .allowCredentials(false)
        .maxAge(3600);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(requestUserIdInterceptor)
        .addPathPatterns("/api/**");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/login").setViewName("forward:/index.html");
    registry.addViewController("/signup").setViewName("forward:/index.html");
    registry.addViewController("/books").setViewName("forward:/index.html");
    registry.addViewController("/books/**").setViewName("forward:/index.html");
    registry.addViewController("/reviews").setViewName("forward:/index.html");
    registry.addViewController("/reviews/**").setViewName("forward:/index.html");
  }
}
