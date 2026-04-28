package com.deokhugam.deokhugam_server.domain.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.NaverBookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.service.BookService;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService bookService;

  @Test
  @DisplayName("도서 등록 성공")
  void createBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();
    LocalDate publishedDate = LocalDate.of(2024, 1, 1);
    LocalDateTime now = LocalDateTime.of(2026, 4, 23, 10, 0);

    MockMultipartFile thumbnailImage = new MockMultipartFile(
      "thumbnailImage",
      "thumbnail.png",
      "image/png",
      "dummy-image".getBytes()
    );

    BookDto response = new BookDto(
      bookId,
      "클린 코드",
      "로버트 마틴",
      "설명",
      "인사이트",
      publishedDate,
      "9788912345678",
      "https://image.test/thumbnail.png",
      0,
      0.0,
      now,
      now
    );

    when(bookService.createBook(any(), any())).thenReturn(response);

    mockMvc.perform(multipart("/api/books")
        .file(thumbnailImage)
        .param("title", "클린 코드")
        .param("author", "로버트 마틴")
        .param("isbn", "978-89-1234-567-8")
        .param("publisher", "인사이트")
        .param("description", "설명")
        .param("publishedDate", "2024-01-01"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(bookId.toString()))
      .andExpect(jsonPath("$.title").value("클린 코드"))
      .andExpect(jsonPath("$.author").value("로버트 마틴"))
      .andExpect(jsonPath("$.description").value("설명"))
      .andExpect(jsonPath("$.publisher").value("인사이트"))
      .andExpect(jsonPath("$.publishedDate").value("2024-01-01"))
      .andExpect(jsonPath("$.isbn").value("9788912345678"))
      .andExpect(jsonPath("$.thumbnailUrl").value("https://image.test/thumbnail.png"));
  }

  @Test
  @DisplayName("도서 목록 조회 성공")
  void getBooks_success() throws Exception {
    UUID bookId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.of(2026, 4, 23, 10, 0);

    BookDto dto = new BookDto(
      bookId,
      "책 제목",
      "저자",
      "설명",
      "출판사",
      LocalDate.of(2024, 1, 1),
      "1234567890",
      null,
      1,
      4.0,
      now,
      now
    );

    CursorPageResponse<BookDto> response = new CursorPageResponse<>(
      List.of(dto),
      null,
      null,
      1,
      1L,
      false
    );

    when(bookService.getBooks(any(BookSearchRequest.class))).thenReturn(response);

    mockMvc.perform(get("/api/books")
        .param("keyword", "책")
        .param("orderBy", "title")
        .param("direction", "DESC")
        .param("limit", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(bookId.toString()))
      .andExpect(jsonPath("$.content[0].title").value("책 제목"))
      .andExpect(jsonPath("$.content[0].author").value("저자"))
      .andExpect(jsonPath("$.content[0].isbn").value("1234567890"))
      .andExpect(jsonPath("$.content[0].reviewCount").value(1))
      .andExpect(jsonPath("$.content[0].rating").value(4.0))
      .andExpect(jsonPath("$.size").value(1))
      .andExpect(jsonPath("$.totalElements").value(1))
      .andExpect(jsonPath("$.hasNext").value(false));
  }

  @Test
  @DisplayName("ISBN OCR 추출 성공")
  void extractIsbn_success() throws Exception {
    MockMultipartFile image = new MockMultipartFile(
      "image",
      "book.png",
      "image/png",
      "dummy-image".getBytes()
    );

    when(bookService.extractIsbn(any())).thenReturn("9788912345678");

    mockMvc.perform(multipart("/api/books/isbn/ocr")
        .file(image))
      .andExpect(status().isOk())
      .andExpect(content().string("9788912345678"));
  }

  @Test
  @DisplayName("도서 정보 조회 성공")
  void getBookInfo_success() throws Exception {
    NaverBookDto response = new NaverBookDto(
      "클린 코드",
      "로버트 마틴",
      "설명",
      "인사이트",
      LocalDate.of(2024, 1, 1),
      "9788912345678",
      "https://image.test/book.png"
    );

    when(bookService.getBookInfo("9788912345678")).thenReturn(response);

    mockMvc.perform(get("/api/books/info")
        .param("isbn", "9788912345678"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.title").value("클린 코드"))
      .andExpect(jsonPath("$.author").value("로버트 마틴"))
      .andExpect(jsonPath("$.description").value("설명"))
      .andExpect(jsonPath("$.publisher").value("인사이트"))
      .andExpect(jsonPath("$.publishedDate").value("2024-01-01"))
      .andExpect(jsonPath("$.isbn").value("9788912345678"))
      .andExpect(jsonPath("$.thumbnailImage").value("https://image.test/book.png"));
  }

  @Test
  @DisplayName("도서 상세 조회 성공")
  void getBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.of(2026, 4, 23, 10, 0);

    BookDto response = new BookDto(
      bookId,
      "책 제목",
      "저자",
      "설명",
      "출판사",
      LocalDate.of(2024, 1, 1),
      "1234567890",
      null,
      3,
      4.5,
      now,
      now
    );

    when(bookService.getBook(bookId)).thenReturn(response);

    mockMvc.perform(get("/api/books/{bookId}", bookId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(bookId.toString()))
      .andExpect(jsonPath("$.title").value("책 제목"))
      .andExpect(jsonPath("$.author").value("저자"))
      .andExpect(jsonPath("$.isbn").value("1234567890"))
      .andExpect(jsonPath("$.reviewCount").value(3))
      .andExpect(jsonPath("$.rating").value(4.5));
  }

  @Test
  @DisplayName("도서 수정 성공")
  void updateBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.of(2026, 4, 23, 10, 0);

    MockMultipartFile thumbnailImage = new MockMultipartFile(
      "thumbnailImage",
      "thumbnail.png",
      "image/png",
      "dummy-image".getBytes()
    );

    BookDto response = new BookDto(
      bookId,
      "수정된 제목",
      "수정된 저자",
      "수정된 설명",
      "수정된 출판사",
      LocalDate.of(2025, 1, 1),
      "1234567890",
      "https://image.test/updated.png",
      0,
      0.0,
      now,
      now
    );

    when(bookService.updateBook(eq(bookId), any(), any())).thenReturn(response);

    mockMvc.perform(multipart("/api/books/{bookId}", bookId)
        .file(thumbnailImage)
        .param("title", "수정된 제목")
        .param("author", "수정된 저자")
        .param("publisher", "수정된 출판사")
        .param("description", "수정된 설명")
        .param("publishedDate", "2025-01-01")
        .with(request -> {
          request.setMethod("PATCH");
          return request;
        }))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(bookId.toString()))
      .andExpect(jsonPath("$.title").value("수정된 제목"))
      .andExpect(jsonPath("$.author").value("수정된 저자"))
      .andExpect(jsonPath("$.description").value("수정된 설명"))
      .andExpect(jsonPath("$.publisher").value("수정된 출판사"))
      .andExpect(jsonPath("$.publishedDate").value("2025-01-01"))
      .andExpect(jsonPath("$.isbn").value("1234567890"));
  }

  @Test
  @DisplayName("도서 논리 삭제 성공")
  void deleteBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();

    doNothing().when(bookService).deleteBook(bookId);

    mockMvc.perform(delete("/api/books/{bookId}", bookId))
      .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("도서 물리 삭제 성공")
  void hardDeleteBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();

    doNothing().when(bookService).hardDeleteBook(bookId);

    mockMvc.perform(delete("/api/books/{bookId}/hard", bookId))
      .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("인기 도서 조회 성공")
  void searchPopularBooks_success() throws Exception {
    UUID popularBookId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 4, 23, 10, 0);

    PopularBookDto dto = new PopularBookDto(
      popularBookId,
      bookId,
      "인기 도서",
      "인기 저자",
      "https://image.test/popular.png",
      Period.DAILY,
      1,
      9.5,
      20,
      4.8,
      createdAt
    );

    CursorPageResponse<PopularBookDto> response = new CursorPageResponse<>(
      List.of(dto),
      null,
      null,
      1,
      1L,
      false
    );

    when(bookService.searchPopularBooks(
      eq(Period.DAILY),
      eq("DESC"),
      ArgumentMatchers.isNull(),
      ArgumentMatchers.isNull(),
      eq(10)
    )).thenReturn(response);

    mockMvc.perform(get("/api/books/popular")
        .param("period", "DAILY")
        .param("direction", "DESC")
        .param("limit", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(popularBookId.toString()))
      .andExpect(jsonPath("$.content[0].bookId").value(bookId.toString()))
      .andExpect(jsonPath("$.content[0].title").value("인기 도서"))
      .andExpect(jsonPath("$.content[0].author").value("인기 저자"))
      .andExpect(jsonPath("$.content[0].rank").value(1))
      .andExpect(jsonPath("$.content[0].score").value(9.5))
      .andExpect(jsonPath("$.content[0].reviewCount").value(20))
      .andExpect(jsonPath("$.content[0].rating").value(4.8));
  }
}
