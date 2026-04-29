package com.deokhugam.deokhugam_server.domain.book.service;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.NaverBookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

  BookDto createBook(BookCreateRequest request, MultipartFile thumbnailImage);

  CursorPageResponse<BookDto> getBooks(BookSearchRequest request);

  String extractIsbn(MultipartFile image);

  BookDto getBook(UUID bookId);

  BookDto updateBook(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage);

  void deleteBook(UUID bookId);

  CursorPageResponse<PopularBookDto> searchPopularBooks(
    Period period, String direction, String cursor, LocalDateTime after, int limit
  );

  NaverBookDto getBookInfo(String isbn);

  void hardDeleteBook(UUID bookId);
}
