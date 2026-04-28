package com.deokhugam.deokhugam_server.domain.book.service;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.NaverBookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.client.BookInfoClient;
import com.deokhugam.deokhugam_server.domain.book.client.TextExtractionClient;
import com.deokhugam.deokhugam_server.domain.book.mapper.BookMapper;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.book.repository.PopularBookRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

  private static final List<String> ALLOWED_ORDER_BY =
    List.of("title", "publisheddate", "rating", "reviewcount");

  private static final List<String> ALLOWED_DIRECTION =
    List.of("ASC", "DESC");

  private static final Pattern ISBN_13_PATTERN = Pattern.compile("\\b97[89][0-9\\- ]{10,20}\\b");
  private static final Pattern ISBN_10_PATTERN = Pattern.compile("\\b[0-9Xx\\- ]{10,20}\\b");

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final PopularBookRepository popularBookRepository;
  private final TextExtractionClient textExtractionClient;
  private final BookInfoClient bookInfoClient;

  @Override
  @Transactional
  public BookDto createBook(BookCreateRequest request, MultipartFile thumbnailImage) {
    String normalizedIsbn = normalizeIsbn(request.isbn());
    validateDuplicateIsbn(normalizedIsbn);

    Book book = new Book(
      request.title().trim(),
      request.author().trim(),
      normalizedIsbn,
      normalizeText(request.publisher()),
      normalizeText(request.description()),
      null,
      request.publishedDate()
    );

    Book savedBook = bookRepository.save(book);
    return getBook(savedBook.getId());
  }

  @Override
  public CursorPageResponse<BookDto> getBooks(BookSearchRequest request) {
    validateBookSearchRequest(request);

    List<BookSearchQueryDto> queryResults = bookRepository.searchBooks(request);
    long totalElements = bookRepository.countBooks(request);

    boolean hasNext = queryResults.size() > request.limit();
    List<BookSearchQueryDto> currentPage = hasNext
      ? queryResults.subList(0, request.limit())
      : queryResults;

    List<BookDto> content = currentPage.stream()
      .map(bookMapper::toDto)
      .toList();

    String nextCursor = null;
    LocalDateTime nextAfter = null;

    if (hasNext && !currentPage.isEmpty()) {
      BookSearchQueryDto lastItem = currentPage.get(currentPage.size() - 1);
      nextCursor = extractNextCursor(lastItem, request.orderBy());
      nextAfter = lastItem.createdAt();
    }

    return new CursorPageResponse<>(
      content,
      nextCursor,
      nextAfter,
      content.size(),
      totalElements,
      hasNext
    );
  }

  @Override
  public String extractIsbn(MultipartFile image) {
    validateImageFile(image);

    String ocrText = textExtractionClient.parseText(image);
    String extractedIsbn = extractIsbnFromText(ocrText);
    if (extractedIsbn == null) {
      throw new DeokhugamException(ErrorCode.ISBN_EXTRACTION_FAILED);
    }

    return extractedIsbn;
  }

  @Override
  public BookDto getBook(UUID bookId) {
    BookSearchQueryDto bookDetail = bookRepository.findBookDetail(bookId);
    if (bookDetail == null) {
      throw new DeokhugamException(ErrorCode.BOOK_NOT_FOUND);
    }

    return bookMapper.toDto(bookDetail);
  }

  @Override
  @Transactional
  public BookDto updateBook(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage) {
    Book book = bookRepository.findByIdAndIsDeletedFalse(bookId)
      .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

    book.update(
      normalizeText(request.title()),
      normalizeText(request.author()),
      normalizeText(request.publisher()),
      normalizeText(request.description()),
      null,
      request.publishedDate()
    );

    return getBook(book.getId());
  }

  @Override
  @Transactional
  public void deleteBook(UUID bookId) {
    Book book = bookRepository.findByIdAndIsDeletedFalse(bookId)
      .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

    book.delete();
  }

  private Integer parseCursorRank(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    try {
      return Integer.parseInt(cursor);
    } catch (NumberFormatException e) {
      throw new DeokhugamException(ErrorCode.INVALID_INPUT_VALUE);
    }
  }

  @Override
  public CursorPageResponse<PopularBookDto> searchPopularBooks(
    Period period, String direction, String cursor, String after, int limit
  ) {
    Integer cursorRank = parseCursorRank(cursor);
    LocalDateTime afterLdt = parseLocalDateTime(after);

    List<PopularBook> popularBooks = popularBookRepository.findPopularBooksWithPaging(
      period, direction.toUpperCase(), cursorRank, afterLdt,
      Limit.of(limit + 1)
    );

    long totalElements = popularBookRepository.countByPeriodType(period);

    boolean hasNext = popularBooks.size() > limit;
    List<PopularBook> content = hasNext ? popularBooks.subList(0, limit) : popularBooks;

    String nextCursor =
      content.isEmpty() ? null : String.valueOf(content.get(content.size() - 1).getRankOrder());
    LocalDateTime nextAfter =
      content.isEmpty() ? null : content.get(content.size() - 1).getCreatedAt();

    return new CursorPageResponse<>(
      content.stream().map(bookMapper::toPopularDto).toList(),
      nextCursor,
      nextAfter,
      content.size(),
      totalElements,
      hasNext
    );
  }

  @Override
  public NaverBookDto getBookInfo(String isbn) {
    String normalizedIsbn = normalizeIsbn(isbn);
    return bookInfoClient.searchByIsbn(normalizedIsbn);
  }

  @Override
  @Transactional
  public void hardDeleteBook(UUID bookId) {
    Book book = bookRepository.findById(bookId)
      .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

    bookRepository.delete(book);
  }

  private void validateDuplicateIsbn(String isbn) {
    if (bookRepository.existsByIsbn(isbn)) {
      throw new DeokhugamException(ErrorCode.DUPLICATE_ISBN);
    }
  }

  private void validateImageFile(MultipartFile image) {
    if (image == null || image.isEmpty()) {
      throw new DeokhugamException(ErrorCode.INVALID_FILE);
    }

    String contentType = image.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new DeokhugamException(ErrorCode.INVALID_FILE_TYPE);
    }
  }

  private String normalizeIsbn(String isbn) {
    if (isbn == null || isbn.isBlank()) {
      throw new DeokhugamException(ErrorCode.INVALID_INPUT_VALUE);
    }

    String normalized = isbn.replace("-", "").replace(" ", "").trim();

    if (!(normalized.length() == 10 || normalized.length() == 13)) {
      throw new DeokhugamException(ErrorCode.INVALID_INPUT_VALUE);
    }

    return normalized;
  }

  private String normalizeText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private void validateBookSearchRequest(BookSearchRequest request) {
    String normalizedOrderBy = normalizeOrderBy(request.orderBy());
    String normalizedDirection = normalizeDirection(request.direction());

    if (!ALLOWED_ORDER_BY.contains(normalizedOrderBy)) {
      throw new DeokhugamException(ErrorCode.INVALID_INPUT_VALUE);
    }

    if (!ALLOWED_DIRECTION.contains(normalizedDirection)) {
      throw new DeokhugamException(ErrorCode.INVALID_INPUT_VALUE);
    }

    boolean hasCursor = request.cursor() != null && !request.cursor().isBlank();
    boolean hasAfter = request.after() != null;

    if (hasCursor != hasAfter) {
      throw new DeokhugamException(ErrorCode.INVALID_INPUT_VALUE);
    }
  }

  private String extractNextCursor(BookSearchQueryDto item, String orderBy) {
    String normalizedOrderBy = normalizeOrderBy(orderBy);

    return switch (normalizedOrderBy) {
      case "publisheddate" -> item.publishedDate() == null ? null : item.publishedDate().toString();
      case "rating" -> String.valueOf(item.rating());
      case "reviewcount" -> String.valueOf(item.reviewCount());
      case "title" -> item.title();
      default -> item.title();
    };
  }

  private LocalDateTime parseLocalDateTime(String after) {
    if (after == null || after.isBlank()) {
      return null;
    }

    try {
      ZoneId kstZone = ZoneId.of("Asia/Seoul");
      if (after.endsWith("Z")) {
        return LocalDateTime.ofInstant(Instant.parse(after), kstZone);
      }
      return LocalDateTime.parse(after);
    } catch (Exception e) {
      return LocalDate.parse(after.substring(0, 10)).atStartOfDay();
    }
  }

  private String normalizeOrderBy(String orderBy) {
    if (orderBy == null || orderBy.isBlank()) {
      return "title";
    }
    return orderBy.trim().toLowerCase();
  }

  private String normalizeDirection(String direction) {
    if (direction == null || direction.isBlank()) {
      return "DESC";
    }
    return direction.trim().toUpperCase();
  }

  private String extractIsbnFromText(String text) {
    Matcher isbn13Matcher = ISBN_13_PATTERN.matcher(text);
    if (isbn13Matcher.find()) {
      String normalized = isbn13Matcher.group().replace("-", "").replace(" ", "");
      if (normalized.length() == 13) {
        return normalized;
      }
    }

    Matcher isbn10Matcher = ISBN_10_PATTERN.matcher(text);
    if (isbn10Matcher.find()) {
      String normalized = isbn10Matcher.group().replace("-", "").replace(" ", "");
      if (normalized.length() == 10) {
        return normalized;
      }
    }

    return null;
  }
}
