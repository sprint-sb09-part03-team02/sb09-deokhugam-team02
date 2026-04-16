package com.deokhugam.deokhugam_server.domain.book.service;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import java.util.UUID;

public interface BookService {

    BookDto createBook(BookCreateRequest request);

    BookDto getBook(UUID bookId);

    BookDto updateBook(UUID bookId, BookUpdateRequest request);

    void deleteBook(UUID bookId);

    void hardDeleteBook(UUID bookId);
}