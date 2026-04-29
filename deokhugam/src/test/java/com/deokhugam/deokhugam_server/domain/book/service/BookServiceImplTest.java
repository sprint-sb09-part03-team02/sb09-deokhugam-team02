package com.deokhugam.deokhugam_server.domain.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.deokhugam_server.domain.book.client.BookInfoClient;
import com.deokhugam.deokhugam_server.domain.book.client.TextExtractionClient;
import com.deokhugam.deokhugam_server.domain.book.client.TextExtractionResult;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.NaverBookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.mapper.BookMapper;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.book.repository.PopularBookRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

  @Mock
  private BookRepository bookRepository;

  @Mock
  private PopularBookRepository popularBookRepository;

  @Mock
  private BookMapper bookMapper;

  @Mock
  private TextExtractionClient textExtractionClient;

  @Mock
  private BookInfoClient bookInfoClient;

  @InjectMocks
  private BookServiceImpl bookService;

  @Test
  @DisplayName("도서 생성 성공")
  void createBook_success() {
    UUID bookId = UUID.randomUUID();
    LocalDate publishedDate = LocalDate.of(2024, 1, 1);
    LocalDateTime now = LocalDateTime.now();

    BookCreateRequest request = new BookCreateRequest(
      "클린 코드",
      "로버트 마틴",
      "978-89-1234-567-8",
      "인사이트",
      "설명",
      publishedDate
    );

    Book savedBook = mock(Book.class);

    BookSearchQueryDto queryDto = new BookSearchQueryDto(
      bookId,
      "클린 코드",
      "로버트 마틴",
      "설명",
      "인사이트",
      publishedDate,
      "9788912345678",
      null,
      0L,
      0.0,
      now,
      now
    );

    BookDto expectedDto = new BookDto(
      bookId,
      "클린 코드",
      "로버트 마틴",
      "설명",
      "인사이트",
      publishedDate,
      "9788912345678",
      null,
      0,
      0.0,
      now,
      now
    );

    when(bookRepository.existsByIsbn("9788912345678")).thenReturn(false);
    when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
    when(savedBook.getId()).thenReturn(bookId);
    when(bookRepository.findBookDetail(bookId)).thenReturn(queryDto);
    when(bookMapper.toDto(queryDto)).thenReturn(expectedDto);

    BookDto result = bookService.createBook(request, null);

    assertNotNull(result);
    assertEquals(expectedDto.title(), result.title());
    assertEquals(expectedDto.isbn(), result.isbn());
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  @DisplayName("도서 생성 실패 - ISBN 중복")
  void createBook_fail_duplicateIsbn() {
    BookCreateRequest request = new BookCreateRequest(
      "클린 코드",
      "로버트 마틴",
      "978-89-1234-567-8",
      "인사이트",
      "설명",
      LocalDate.of(2024, 1, 1)
    );

    when(bookRepository.existsByIsbn("9788912345678")).thenReturn(true);

    DeokhugamException exception = assertThrows(DeokhugamException.class, () ->
      bookService.createBook(request, null)
    );

    assertEquals(ErrorCode.DUPLICATE_ISBN, exception.getErrorCode());
  }

  @Test
  @DisplayName("도서 상세 조회 성공")
  void getBook_success() {
    UUID bookId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    BookSearchQueryDto queryDto = new BookSearchQueryDto(
      bookId,
      "책 제목",
      "저자",
      "설명",
      "출판사",
      LocalDate.of(2024, 1, 1),
      "1234567890",
      null,
      3L,
      4.5,
      now,
      now
    );

    BookDto expectedDto = new BookDto(
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

    when(bookRepository.findBookDetail(bookId)).thenReturn(queryDto);
    when(bookMapper.toDto(queryDto)).thenReturn(expectedDto);

    BookDto result = bookService.getBook(bookId);

    assertNotNull(result);
    assertEquals(expectedDto.id(), result.id());
    assertEquals(expectedDto.title(), result.title());
  }

  @Test
  @DisplayName("도서 상세 조회 실패 - 없는 도서")
  void getBook_fail_notFound() {
    UUID bookId = UUID.randomUUID();

    when(bookRepository.findBookDetail(bookId)).thenReturn(null);

    DeokhugamException exception = assertThrows(DeokhugamException.class, () ->
      bookService.getBook(bookId)
    );

    assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("도서 목록 조회 성공")
  void getBooks_success() {
    UUID bookId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    BookSearchRequest request = new BookSearchRequest(
      null,
      null,
      null,
      null,
      null,
      10
    );

    BookSearchQueryDto queryDto = new BookSearchQueryDto(
      bookId,
      "책 제목",
      "저자",
      "설명",
      "출판사",
      LocalDate.of(2024, 1, 1),
      "1234567890",
      null,
      1L,
      4.0,
      now,
      now
    );

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

    when(bookRepository.searchBooks(request)).thenReturn(List.of(queryDto));
    when(bookRepository.countBooks(request)).thenReturn(1L);
    when(bookMapper.toDto(queryDto)).thenReturn(dto);

    CursorPageResponse<BookDto> result = bookService.getBooks(request);

    assertNotNull(result);
    assertEquals(1, result.content().size());
    assertEquals(1L, result.totalElements());
    assertFalse(result.hasNext());
    assertNull(result.nextCursor());
    assertNull(result.nextAfter());
  }

  @Test
  @DisplayName("도서 수정 성공")
  void updateBook_success() {
    UUID bookId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    Book book = mock(Book.class);

    BookUpdateRequest request = new BookUpdateRequest(
      "수정된 제목",
      "수정된 저자",
      "수정된 출판사",
      "수정된 설명",
      LocalDate.of(2025, 1, 1)
    );

    BookSearchQueryDto queryDto = new BookSearchQueryDto(
      bookId,
      "수정된 제목",
      "수정된 저자",
      "수정된 설명",
      "수정된 출판사",
      LocalDate.of(2025, 1, 1),
      "1234567890",
      null,
      0L,
      0.0,
      now,
      now
    );

    BookDto expectedDto = new BookDto(
      bookId,
      "수정된 제목",
      "수정된 저자",
      "수정된 설명",
      "수정된 출판사",
      LocalDate.of(2025, 1, 1),
      "1234567890",
      null,
      0,
      0.0,
      now,
      now
    );

    when(bookRepository.findByIdAndIsDeletedFalse(bookId)).thenReturn(Optional.of(book));
    when(book.getId()).thenReturn(bookId);
    when(bookRepository.findBookDetail(bookId)).thenReturn(queryDto);
    when(bookMapper.toDto(queryDto)).thenReturn(expectedDto);

    BookDto result = bookService.updateBook(bookId, request, null);

    assertNotNull(result);
    assertEquals(expectedDto.title(), result.title());

    verify(book).update(
      eq("수정된 제목"),
      eq("수정된 저자"),
      eq("수정된 출판사"),
      eq("수정된 설명"),
      eq(null),
      eq(LocalDate.of(2025, 1, 1))
    );
  }

  @Test
  @DisplayName("도서 수정 시 ISBN은 변경되지 않는다")
  void updateBook_isbnNotChanged() {
    UUID bookId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    Book book = mock(Book.class);

    BookUpdateRequest request = new BookUpdateRequest(
      "수정된 제목",
      "수정된 저자",
      "수정된 출판사",
      "수정된 설명",
      LocalDate.of(2025, 1, 1)
    );

    BookSearchQueryDto queryDto = new BookSearchQueryDto(
      bookId,
      "수정된 제목",
      "수정된 저자",
      "수정된 설명",
      "수정된 출판사",
      LocalDate.of(2025, 1, 1),
      "1234567890",
      null,
      0L,
      0.0,
      now,
      now
    );

    BookDto expectedDto = new BookDto(
      bookId,
      "수정된 제목",
      "수정된 저자",
      "수정된 설명",
      "수정된 출판사",
      LocalDate.of(2025, 1, 1),
      "1234567890",
      null,
      0,
      0.0,
      now,
      now
    );

    when(bookRepository.findByIdAndIsDeletedFalse(bookId)).thenReturn(Optional.of(book));
    when(book.getId()).thenReturn(bookId);
    when(bookRepository.findBookDetail(bookId)).thenReturn(queryDto);
    when(bookMapper.toDto(queryDto)).thenReturn(expectedDto);

    BookDto result = bookService.updateBook(bookId, request, null);

    assertNotNull(result);
    assertEquals("1234567890", result.isbn());

    verify(book).update(
      eq("수정된 제목"),
      eq("수정된 저자"),
      eq("수정된 출판사"),
      eq("수정된 설명"),
      eq(null),
      eq(LocalDate.of(2025, 1, 1))
    );
  }

  @Test
  @DisplayName("도서 수정 실패 - 없는 도서")
  void updateBook_fail_notFound() {
    UUID bookId = UUID.randomUUID();

    BookUpdateRequest request = new BookUpdateRequest(
      "수정된 제목",
      "수정된 저자",
      "수정된 출판사",
      "수정된 설명",
      LocalDate.of(2025, 1, 1)
    );

    when(bookRepository.findByIdAndIsDeletedFalse(bookId)).thenReturn(Optional.empty());

    DeokhugamException exception = assertThrows(DeokhugamException.class, () ->
      bookService.updateBook(bookId, request, null)
    );

    assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("도서 논리 삭제 성공")
  void deleteBook_success() {
    UUID bookId = UUID.randomUUID();
    Book book = mock(Book.class);

    when(bookRepository.findByIdAndIsDeletedFalse(bookId)).thenReturn(Optional.of(book));

    bookService.deleteBook(bookId);

    verify(book).delete();
  }

  @Test
  @DisplayName("도서 논리 삭제 실패 - 없는 도서")
  void deleteBook_fail_notFound() {
    UUID bookId = UUID.randomUUID();

    when(bookRepository.findByIdAndIsDeletedFalse(bookId)).thenReturn(Optional.empty());

    DeokhugamException exception = assertThrows(DeokhugamException.class, () ->
      bookService.deleteBook(bookId)
    );

    assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("도서 물리 삭제 성공")
  void hardDeleteBook_success() {
    UUID bookId = UUID.randomUUID();
    Book book = mock(Book.class);

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    bookService.hardDeleteBook(bookId);

    verify(bookRepository).delete(book);
  }

  @Test
  @DisplayName("도서 물리 삭제 실패 - 없는 도서")
  void hardDeleteBook_fail_notFound() {
    UUID bookId = UUID.randomUUID();

    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    DeokhugamException exception = assertThrows(DeokhugamException.class, () ->
      bookService.hardDeleteBook(bookId)
    );

    assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("ISBN 추출 성공 - OCR 결과에서 ISBN 13 추출")
  void extractIsbn_success_isbn13() {
    MockMultipartFile image = new MockMultipartFile(
      "image",
      "book.png",
      "image/png",
      "dummy".getBytes()
    );

    when(textExtractionClient.extractText(image))
        .thenReturn(new TextExtractionResult("ISBN 978-89-1234-567-8", "OCR_SPACE"));

    String result = bookService.extractIsbn(image);

    assertEquals("9788912345678", result);
  }

  @Test
  @DisplayName("ISBN 추출 성공 - OCR 결과에서 ISBN 10 추출")
  void extractIsbn_success_isbn10() {
    MockMultipartFile image = new MockMultipartFile(
      "image",
      "book.jpg",
      "image/jpeg",
      "dummy".getBytes()
    );

    when(textExtractionClient.extractText(image))
        .thenReturn(new TextExtractionResult("ISBN 89-1234-567X", "OCR_SPACE"));

    String result = bookService.extractIsbn(image);

    assertEquals("891234567X", result);
  }

  @Test
  @DisplayName("ISBN 추출 실패 - 파일이 비어있음")
  void extractIsbn_fail_invalidFile() {
    MockMultipartFile image = new MockMultipartFile(
      "image",
      "book.png",
      "image/png",
      new byte[0]
    );

    DeokhugamException exception = assertThrows(DeokhugamException.class, () ->
      bookService.extractIsbn(image)
    );

    assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
  }

  @Test
  @DisplayName("ISBN 추출 실패 - 잘못된 파일 타입")
  void extractIsbn_fail_invalidFileType() {
    MockMultipartFile image = new MockMultipartFile(
      "image",
      "book.txt",
      "text/plain",
      "dummy".getBytes()
    );

    DeokhugamException exception = assertThrows(DeokhugamException.class, () ->
      bookService.extractIsbn(image)
    );

    assertEquals(ErrorCode.INVALID_FILE_TYPE, exception.getErrorCode());
  }

  @Test
  @DisplayName("ISBN 추출 실패 - OCR 결과에 ISBN 없음")
  void extractIsbn_fail_noIsbnInOcrText() {
    MockMultipartFile image = new MockMultipartFile(
      "image",
      "random-book-cover.png",
      "image/png",
      "dummy".getBytes()
    );

    when(textExtractionClient.extractText(image))
        .thenReturn(new TextExtractionResult("no isbn text", "OCR_SPACE"));

    DeokhugamException exception = assertThrows(DeokhugamException.class, () ->
      bookService.extractIsbn(image)
    );

    assertEquals(ErrorCode.ISBN_EXTRACTION_FAILED, exception.getErrorCode());
  }

  @Test
  @DisplayName("도서 정보 조회 성공 - 네이버 API 조회")
  void getBookInfo_success() {
    String isbn = "978-89-1234-567-8";
    String normalizedIsbn = "9788912345678";

    NaverBookDto expected = new NaverBookDto(
      "클린 코드",
      "로버트 마틴",
      "설명",
      "인사이트",
      LocalDate.of(2024, 1, 1),
      normalizedIsbn,
      "https://image.test/book.png"
    );

    when(bookInfoClient.searchByIsbn(normalizedIsbn)).thenReturn(expected);

    NaverBookDto result = bookService.getBookInfo(isbn);

    assertNotNull(result);
    assertEquals("클린 코드", result.title());
    assertEquals("로버트 마틴", result.author());
    assertEquals(normalizedIsbn, result.isbn());
    assertEquals("https://image.test/book.png", result.thumbnailImage());
  }

  @Test
  @DisplayName("도서 정보 조회 실패 - 네이버 API 조회 결과 없음")
  void getBookInfo_fail_notFound() {
    String isbn = "978-89-1234-567-8";
    String normalizedIsbn = "9788912345678";

    when(bookInfoClient.searchByIsbn(normalizedIsbn))
      .thenThrow(new DeokhugamException(ErrorCode.BOOK_INFO_NOT_FOUND));

    DeokhugamException exception = assertThrows(DeokhugamException.class, () ->
      bookService.getBookInfo(isbn)
    );

    assertEquals(ErrorCode.BOOK_INFO_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("인기 도서 조회 성공")
  void searchPopularBooks_success() {
    Period period = Period.DAILY;
    String direction = "DESC";
    String cursor = null;
    int limit = 10;

    PopularBook popularBook = mock(PopularBook.class);

    PopularBookDto dto = new PopularBookDto(
      UUID.randomUUID(),
      UUID.randomUUID(),
      "인기 도서",
      "인기 저자",
      null,
      period,
      1,
      9.0,
      10,
      4.5,
      LocalDateTime.now()
    );

    when(popularBookRepository.findPopularBooksDynamic(any(), any(), any(), anyString(), anyInt(), any(LocalDate.class)))
      .thenReturn(List.of(popularBook));
    when(bookMapper.toPopularDto(popularBook)).thenReturn(dto);

    CursorPageResponse<PopularBookDto> result =
      bookService.searchPopularBooks(period, direction, cursor, LocalDateTime.now(), limit);

    assertNotNull(result);
    assertEquals(1, result.content().size());
    assertEquals("인기 도서", result.content().get(0).title());
    assertFalse(result.hasNext());
  }

  @Test
  @DisplayName("인기 도서 조회 성공 - 다음 페이지 존재")
  void searchPopularBooks_success_hasNext() {
    Period period = Period.DAILY;
    String direction = "DESC";

    PopularBook first = mock(PopularBook.class);
    PopularBook second = mock(PopularBook.class);

    LocalDateTime firstCreatedAt = LocalDateTime.of(2026, 4, 20, 10, 0);

    when(first.getRankOrder()).thenReturn(1);
    when(first.getCreatedAt()).thenReturn(firstCreatedAt);

    PopularBookDto firstDto = new PopularBookDto(
      UUID.randomUUID(),
      UUID.randomUUID(),
      "첫 번째 인기 도서",
      "저자1",
      null,
      period,
      1,
      9.5,
      20,
      4.8,
      firstCreatedAt
    );

    when(popularBookRepository.findPopularBooksDynamic(any(), any(), any(), anyString(), anyInt(), any(LocalDate.class)))
      .thenReturn(List.of(first, second));

    when(popularBookRepository.countByPeriodType(period)).thenReturn(2L);
    when(bookMapper.toPopularDto(first)).thenReturn(firstDto);

    CursorPageResponse<PopularBookDto> result =
      bookService.searchPopularBooks(period, direction, null, null, 1);

    assertNotNull(result);
    assertEquals(1, result.content().size());
    assertEquals(2L, result.totalElements());
    assertEquals("1", result.nextCursor());
    assertEquals(firstCreatedAt, result.nextAfter());
    assertTrue(result.hasNext());
  }
}
