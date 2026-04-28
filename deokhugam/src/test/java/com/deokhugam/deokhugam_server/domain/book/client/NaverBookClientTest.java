package com.deokhugam.deokhugam_server.domain.book.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.deokhugam.deokhugam_server.domain.book.config.NaverApiProperties;
import com.deokhugam.deokhugam_server.domain.book.dto.response.NaverBookDto;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class NaverBookClientTest {

  private static final String NAVER_BOOK_URL = "https://openapi.naver.com/v1/search/book_adv.json";

  private MockRestServiceServer server;
  private NaverBookClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    client = new NaverBookClient(
        builder,
        new NaverApiProperties("client-id", "client-secret", NAVER_BOOK_URL)
    );
  }

  @Test
  @DisplayName("성공: ISBN으로 네이버 도서 정보를 조회하고 HTML 태그를 제거한다")
  void searchByIsbn_Success() {
    // given
    String isbn = "9788912345678";
    String response = """
        {
          "items": [
            {
              "title": "<b>클린 코드</b>",
              "image": "https://image.example/book.jpg",
              "author": "로버트 <b>마틴</b>",
              "publisher": "인사이트",
              "pubdate": "20240131",
              "isbn": "978-89-1234-567-8",
              "description": "<b>좋은 코드</b>를 위한 설명"
            }
          ]
        }
        """;

    server.expect(requestTo(NAVER_BOOK_URL + "?d_isbn=" + isbn))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("X-Naver-Client-Id", "client-id"))
        .andExpect(header("X-Naver-Client-Secret", "client-secret"))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    // when
    NaverBookDto result = client.searchByIsbn(isbn);

    // then
    assertThat(result.title()).isEqualTo("클린 코드");
    assertThat(result.author()).isEqualTo("로버트 마틴");
    assertThat(result.description()).isEqualTo("좋은 코드를 위한 설명");
    assertThat(result.publishedDate()).isEqualTo(LocalDate.of(2024, 1, 31));
    assertThat(result.isbn()).isEqualTo(isbn);
    assertThat(result.thumbnailImage()).isEqualTo("https://image.example/book.jpg");
    server.verify();
  }

  @Test
  @DisplayName("성공: 정확히 일치하는 ISBN이 없으면 첫 번째 검색 결과를 사용한다")
  void searchByIsbn_FallbackToFirstItem_Success() {
    // given
    String isbn = "9788912345678";
    String response = """
        {
          "items": [
            {
              "title": "대체 도서",
              "image": null,
              "author": "저자",
              "publisher": "출판사",
              "pubdate": "",
              "isbn": "9791111111111",
              "description": null
            }
          ]
        }
        """;

    server.expect(requestTo(NAVER_BOOK_URL + "?d_isbn=" + isbn))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    // when
    NaverBookDto result = client.searchByIsbn(isbn);

    // then
    assertThat(result.title()).isEqualTo("대체 도서");
    assertThat(result.publishedDate()).isNull();
    assertThat(result.description()).isNull();
    server.verify();
  }

  @Test
  @DisplayName("실패: 검색 결과가 없으면 도서 정보 없음 예외를 던진다")
  void searchByIsbn_Fail_EmptyItems() {
    // given
    String isbn = "9788912345678";
    server.expect(requestTo(NAVER_BOOK_URL + "?d_isbn=" + isbn))
        .andRespond(withSuccess("{\"items\":[]}", MediaType.APPLICATION_JSON));

    // when & then
    assertThatThrownBy(() -> client.searchByIsbn(isbn))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.BOOK_INFO_NOT_FOUND);
    server.verify();
  }

  @Test
  @DisplayName("실패: 네이버 API 호출이 실패하면 도서 정보 없음 예외로 변환한다")
  void searchByIsbn_Fail_ApiException() {
    // given
    String isbn = "9788912345678";
    server.expect(requestTo(NAVER_BOOK_URL + "?d_isbn=" + isbn))
        .andRespond(withException(new IOException("network error")));

    // when & then
    assertThatThrownBy(() -> client.searchByIsbn(isbn))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.BOOK_INFO_NOT_FOUND);
    server.verify();
  }
}
