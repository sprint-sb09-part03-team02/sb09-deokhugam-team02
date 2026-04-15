package com.deokhugam.deokhugam_server.domain.book.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookDto(
        UUID id,
        String title,
        String author,
        String isbn,
        String publisher,
        String description,
        String imageUrl,
        LocalDate publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}