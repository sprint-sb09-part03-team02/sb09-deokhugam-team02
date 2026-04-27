package com.deokhugam.deokhugam_server.global.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StaticImagePathMapperTest {

  private final StaticImagePathMapper mapper = new StaticImagePathMapper();

  @Test
  @DisplayName("정적 이미지 경로는 프론트가 /images prefix를 붙일 수 있도록 상대 경로로 변환한다")
  void normalizeStaticImagePath_staticImagePath() {
    assertThat(mapper.normalizeStaticImagePath("/images/books/imgError.png"))
        .isEqualTo("books/imgError.png");
    assertThat(mapper.normalizeStaticImagePath("/books/imgError.png"))
        .isEqualTo("books/imgError.png");
    assertThat(mapper.normalizeStaticImagePath("images/books/imgError.png"))
        .isEqualTo("books/imgError.png");
  }

  @Test
  @DisplayName("S3 같은 외부 URL은 그대로 유지한다")
  void normalizeStaticImagePath_externalUrl() {
    String url = "https://bucket.s3.ap-northeast-2.amazonaws.com/books/image.png";

    assertThat(mapper.normalizeStaticImagePath(url)).isEqualTo(url);
  }

  @Test
  @DisplayName("값이 없거나 공백이면 원본 값을 유지한다")
  void normalizeStaticImagePath_emptyValue() {
    assertThat(mapper.normalizeStaticImagePath(null)).isNull();
    assertThat(mapper.normalizeStaticImagePath("")).isEmpty();
    assertThat(mapper.normalizeStaticImagePath("   ")).isBlank();
  }
}
