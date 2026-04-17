package com.deokhugam.deokhugam_server.domain.book.service;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.List;
import java.util.UUID;

public interface BookService {

    BookDto createBook(BookCreateRequest request);

    BookDto getBook(UUID bookId);

    BookDto updateBook(UUID bookId, BookUpdateRequest request);

    void deleteBook(UUID bookId);

    void hardDeleteBook(UUID bookId);

    List<PopularBookDto> searchPopularBooks(Period period, String direction, String cursor, String after, int limit);

}