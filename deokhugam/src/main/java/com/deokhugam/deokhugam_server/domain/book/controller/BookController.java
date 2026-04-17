package com.deokhugam.deokhugam_server.domain.book.controller;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.service.BookService;
import com.deokhugam.deokhugam_server.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ApiResponse<BookDto> createBook(@Valid @RequestBody BookCreateRequest request) {
        return ApiResponse.success(bookService.createBook(request), HttpStatus.CREATED);
    }

    @GetMapping("/{bookId}")
    public ApiResponse<BookDto> getBook(@PathVariable UUID bookId) {
        return ApiResponse.success(bookService.getBook(bookId));
    }

    @PatchMapping("/{bookId}")
    public ApiResponse<BookDto> updateBook(
            @PathVariable UUID bookId,
            @Valid @RequestBody BookUpdateRequest request
    ) {
        return ApiResponse.success(bookService.updateBook(bookId, request));
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
}