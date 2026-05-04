package com.deokhugam.deokhugam_server.domain.book.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookEntityTest {

  @Test
  @DisplayName("도서를 생성하면 기본 리뷰 수와 평점은 0으로 초기화된다")
  void constructor_success() {
    Book book = new Book(
      "클린 코드",
      "로버트 마틴",
      "9788966260959",
      "인사이트",
      "좋은 코드 작성법",
      "thumbnail.jpg",
      LocalDate.of(2013, 12, 24)
    );

    assertThat(book.getTitle()).isEqualTo("클린 코드");
    assertThat(book.getTitleSortKey()).isEqualTo(Book.toTitleSortKey("클린 코드"));
    assertThat(book.getAuthor()).isEqualTo("로버트 마틴");
    assertThat(book.getIsbn()).isEqualTo("9788966260959");
    assertThat(book.getPublisher()).isEqualTo("인사이트");
    assertThat(book.getDescription()).isEqualTo("좋은 코드 작성법");
    assertThat(book.getThumbnailUrl()).isEqualTo("thumbnail.jpg");
    assertThat(book.getPublishedDate()).isEqualTo(LocalDate.of(2013, 12, 24));
    assertThat(book.getReviewCount()).isZero();
    assertThat(book.getRating()).isZero();
  }

  @Test
  @DisplayName("도서 정보를 수정한다")
  void update_success() {
    Book book = createBook();

    book.update(
      "수정된 제목",
      "수정된 저자",
      "수정된 출판사",
      "수정된 설명",
      "updated-thumbnail.jpg",
      LocalDate.of(2025, 1, 1)
    );

    assertThat(book.getTitle()).isEqualTo("수정된 제목");
    assertThat(book.getTitleSortKey()).isEqualTo(Book.toTitleSortKey("수정된 제목"));
    assertThat(book.getAuthor()).isEqualTo("수정된 저자");
    assertThat(book.getPublisher()).isEqualTo("수정된 출판사");
    assertThat(book.getDescription()).isEqualTo("수정된 설명");
    assertThat(book.getThumbnailUrl()).isEqualTo("updated-thumbnail.jpg");
    assertThat(book.getPublishedDate()).isEqualTo(LocalDate.of(2025, 1, 1));
  }

  @Test
  @DisplayName("도서 수정 시 null 값은 기존 값을 유지한다")
  void update_nullValues_keepOriginalValues() {
    Book book = createBook();

    book.update(null, null, null, null, null, null);

    assertThat(book.getTitle()).isEqualTo("클린 코드");
    assertThat(book.getTitleSortKey()).isEqualTo(Book.toTitleSortKey("클린 코드"));
    assertThat(book.getAuthor()).isEqualTo("로버트 마틴");
    assertThat(book.getPublisher()).isEqualTo("인사이트");
    assertThat(book.getDescription()).isEqualTo("좋은 코드 작성법");
    assertThat(book.getThumbnailUrl()).isEqualTo("thumbnail.jpg");
    assertThat(book.getPublishedDate()).isEqualTo(LocalDate.of(2013, 12, 24));
  }

  @Test
  @DisplayName("리뷰 통계를 수정한다")
  void updateReviewStatistics_success() {
    Book book = createBook();

    book.updateReviewStatistics(10, 4.5);

    assertThat(book.getReviewCount()).isEqualTo(10);
    assertThat(book.getRating()).isEqualTo(4.5);
  }

  @Test
  @DisplayName("정렬용 제목 키는 대소문자, 구두점, 숫자 자릿수를 정규화한다")
  void toTitleSortKey_mixedTitle_success() {
    assertThat(Book.toTitleSortKey(" Book-10 ")).isEqualTo(Book.toTitleSortKey("book 10"));
    assertThat(Book.toTitleSortKey("book 2")).isLessThan(Book.toTitleSortKey("Book-10"));
    assertThat(Book.toTitleSortKey("정렬-Banana")).contains("1b1a1n1a1n1a");
    assertThat(Book.toTitleSortKey("내 강아지")).startsWith("2020100");
  }

  @Test
  @DisplayName("정렬용 제목 키는 숫자, 영문, 한글, 기타 문자를 그룹 순서대로 정렬한다")
  void toTitleSortKey_characterGroupOrder_success() {
    assertThat(Book.toTitleSortKey("1984")).startsWith("0");
    assertThat(Book.toTitleSortKey("Money")).startsWith("1");
    assertThat(Book.toTitleSortKey("총 균 쇠")).startsWith("2");
    assertThat(Book.toTitleSortKey("☆별")).startsWith("9");
  }

  private Book createBook() {
    return new Book(
      "클린 코드",
      "로버트 마틴",
      "9788966260959",
      "인사이트",
      "좋은 코드 작성법",
      "thumbnail.jpg",
      LocalDate.of(2013, 12, 24)
    );
  }
}
