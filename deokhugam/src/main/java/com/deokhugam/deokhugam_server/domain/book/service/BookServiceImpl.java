package com.deokhugam.deokhugam_server.domain.book.service;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.mapper.BookMapper;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookDto createBook(BookCreateRequest request) {
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
        return toBookDto(savedBook);
    }

    @Override
    public BookDto getBook(UUID bookId) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(bookId)
                .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

        return toBookDto(book);
    }

    @Override
    @Transactional
    public BookDto updateBook(UUID bookId, BookUpdateRequest request) {
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

        return toBookDto(book);
    }

    @Override
    @Transactional
    public void deleteBook(UUID bookId) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(bookId)
                .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

        book.delete();
    }

    @Override
    @Transactional
    public void hardDeleteBook(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

        bookRepository.delete(book);
    }

    @Override
    public List<PopularBookDto> searchPopularBooks(Period period, String direction, String cursor,
        String after, int limit) {
        LocalDateTime startTime = calculateStartTime(period);
        List<PopularBook> popularBooks = bookRepository.findPopularBooksWithPaging(
            startTime, direction, cursor, after, limit
        );
        return popularBooks.stream()
            .map(bookMapper::toPopularDto)
            .collect(Collectors.toList());
    }

    private BookDto toBookDto(Book book) {
        return bookMapper.toDto(book, 0, 0.0);
    }

    private void validateDuplicateIsbn(String isbn) {
        if (isbn == null) {
            return;
        }

        if (bookRepository.existsByIsbnAndIsDeletedFalse(isbn)) {
            throw new DeokhugamException(ErrorCode.DUPLICATE_ISBN);
        }
    }

    private String normalizeIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return null;
        }

        return isbn.replace("-", "").trim();
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private LocalDateTime calculateStartTime(Period period) {
        return switch (period) {
            case DAILY -> LocalDateTime.now().minusDays(1);
            case WEEKLY -> LocalDateTime.now().minusWeeks(1);
            case MONTHLY -> LocalDateTime.now().minusMonths(1);
            case ALL_TIME -> LocalDateTime.of(2020, 1, 1, 0, 0); // 아주 오래전 시간
        };
    }
}