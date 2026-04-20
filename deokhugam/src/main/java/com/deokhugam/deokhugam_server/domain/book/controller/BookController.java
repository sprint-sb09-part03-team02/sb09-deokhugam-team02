package com.deokhugam.deokhugam_server.domain.book.controller;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.service.BookService;
import com.deokhugam.deokhugam_server.global.response.ApiResponse;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;

  @PostMapping(consumes = "multipart/form-data")
  public ApiResponse<BookDto> createBook(
          @Valid @ModelAttribute BookCreateRequest request,
          @RequestParam(required = false) MultipartFile thumbnailImage
  ) {
    BookDto response = bookService.createBook(request, thumbnailImage);
    return ApiResponse.success(response, HttpStatus.CREATED);
  }

  @GetMapping
  public ApiResponse<CursorPageResponse<BookDto>> getBooks(
          @Valid @ModelAttribute BookSearchRequest request
  ) {
    CursorPageResponse<BookDto> response = bookService.getBooks(request);
    return ApiResponse.success(response);
  }

  @GetMapping("/{bookId}")
  public ApiResponse<BookDto> getBook(@PathVariable UUID bookId) {
    BookDto response = bookService.getBook(bookId);
    return ApiResponse.success(response);
  }

  @PatchMapping(value = "/{bookId}", consumes = "multipart/form-data")
  public ApiResponse<BookDto> updateBook(
          @PathVariable UUID bookId,
          @Valid @ModelAttribute BookUpdateRequest request,
          @RequestParam(required = false) MultipartFile thumbnailImage
  ) {
    BookDto response = bookService.updateBook(bookId, request, thumbnailImage);
    return ApiResponse.success(response);
  }

  @DeleteMapping("/{bookId}")
  public ApiResponse<Void> deleteBook(@PathVariable UUID bookId) {
    bookService.deleteBook(bookId);
    return ApiResponse.success(null, HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{bookId}/hard")
  public ApiResponse<Void> hardDeleteBook(@PathVariable UUID bookId) {
    bookService.hardDeleteBook(bookId);
    return ApiResponse.success(null, HttpStatus.NO_CONTENT);
  }

  @GetMapping("/popular")
  public ApiResponse<CursorPageResponse<PopularBookDto>> searchPopularBooks(
          @RequestParam(defaultValue = "DAILY") Period period,
          @RequestParam(defaultValue = "ASC") String direction,
          @RequestParam(required = false) String cursor,
          @RequestParam(required = false) String after,
          @RequestParam(defaultValue = "50") int limit
  ) {
    CursorPageResponse<PopularBookDto> popularBooks = bookService.searchPopularBooks(
            period, direction, cursor, after, limit
    );
    return ApiResponse.success(popularBooks);
  }
}