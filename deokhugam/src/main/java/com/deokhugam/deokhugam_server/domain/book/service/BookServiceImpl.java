package com.deokhugam.deokhugam_server.domain.book.service;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

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
                request.publishedAt()
        );

        Book savedBook = bookRepository.save(book);
        return toDto(savedBook);
    }

    @Override
    public BookDto getBook(UUID bookId) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(bookId)
                .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

        return toDto(book);
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
                request.publishedAt()
        );

        return toDto(book);
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

    private BookDto toDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublisher(),
                book.getDescription(),
                book.getImageUrl(),
                book.getPublishedAt(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}