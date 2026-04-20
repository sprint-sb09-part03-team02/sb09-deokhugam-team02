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
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

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

    @Override
    @Transactional
    public void hardDeleteBook(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

        bookRepository.delete(book);
    }

    @Override
    public List<PopularBookDto> searchPopularBooks(
            Period period,
            String direction,
            String cursor,
            String after,
            int limit
    ) {
        validatePopularSearchDirection(direction);

        List<PopularBook> popularBooks = bookRepository.findPopularBooksWithPaging(
                period,
                direction,
                cursor,
                after,
                limit
        );

        boolean hasNext = popularBooks.size() > limit;
        List<PopularBook> currentPage = hasNext
                ? popularBooks.subList(0, limit)
                : popularBooks;

        return currentPage.stream()
                .map(bookMapper::toPopularDto)
                .toList();
    }

    private void validateDuplicateIsbn(String isbn) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw new DeokhugamException(ErrorCode.DUPLICATE_ISBN);
        }
    }

    private String normalizeIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new DeokhugamException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return isbn.replace("-", "").trim();
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

    private void validatePopularSearchDirection(String direction) {
        String normalizedDirection = normalizeDirection(direction);

        if (!ALLOWED_DIRECTION.contains(normalizedDirection)) {
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
}