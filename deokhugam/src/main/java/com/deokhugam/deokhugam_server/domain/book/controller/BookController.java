package com.deokhugam.deokhugam_server.domain.book.controller;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.service.BookService;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<BookDto> createBook(
          @Valid @ModelAttribute BookCreateRequest request,
          @RequestParam(required = false) MultipartFile thumbnailImage
  ) {
    BookDto response = bookService.createBook(request, thumbnailImage);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<BookDto>> getBooks(
          @Valid @ModelAttribute BookSearchRequest request
  ) {
    CursorPageResponse<BookDto> response = bookService.getBooks(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{bookId}")
  public ResponseEntity<BookDto> getBook(@PathVariable UUID bookId) {
    BookDto response = bookService.getBook(bookId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping(value = "/{bookId}", consumes = "multipart/form-data")
  public ResponseEntity<BookDto> updateBook(
          @PathVariable UUID bookId,
          @Valid @ModelAttribute BookUpdateRequest request,
          @RequestParam(required = false) MultipartFile thumbnailImage
  ) {
    BookDto response = bookService.updateBook(bookId, request, thumbnailImage);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{bookId}")
  public ResponseEntity<Void> deleteBook(@PathVariable UUID bookId) {
    bookService.deleteBook(bookId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{bookId}/hard")
  public ResponseEntity<Void> hardDeleteBook(@PathVariable UUID bookId) {
    bookService.hardDeleteBook(bookId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/popular")
  public ResponseEntity<CursorPageResponse<PopularBookDto>> searchPopularBooks(
          @RequestParam(defaultValue = "DAILY") Period period,
          @RequestParam(defaultValue = "ASC") String direction,
          @RequestParam(required = false) String cursor,
          @RequestParam(required = false) String after,
          @RequestParam(defaultValue = "50") int limit
  ) {
    CursorPageResponse<PopularBookDto> popularBooks = bookService.searchPopularBooks(
            period, direction, cursor, after, limit
    );
    return ResponseEntity.ok(popularBooks);
  }
}
