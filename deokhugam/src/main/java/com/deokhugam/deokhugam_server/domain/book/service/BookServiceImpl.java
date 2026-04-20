package com.deokhugam.deokhugam_server.domain.book.service;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final PopularBookRepository popularBookRepository;

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
        return toBookDto(savedBook);
    }

    @Override
    public CursorPageResponse<BookDto> getBooks(BookSearchRequest request) {
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
    public BookDto getBook(UUID bookId) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(bookId)
                .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

        return toBookDto(book);
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
    public CursorPageResponse<PopularBookDto> searchPopularBooks(
            Period period, String direction, String cursor, String after, int limit
    ) {
        Integer cursorRank = (cursor != null && !cursor.isBlank()) ? Integer.parseInt(cursor) : null;
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
            nextCursor, nextAfter, content.size(), totalElements, hasNext
        );
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

    private String extractNextCursor(BookSearchQueryDto item, String orderBy) {
        return switch (orderBy) {
            case "publishedDate" -> item.publishedDate() == null ? "" : item.publishedDate().toString();
            case "rating" -> String.valueOf(item.rating());
            case "reviewCount" -> String.valueOf(item.reviewCount());
            case "title" -> item.title();
            default -> item.title();
        };
    }
    private LocalDateTime parseLocalDateTime(String after) {
        if (after == null || after.isBlank())
            return null;
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
}