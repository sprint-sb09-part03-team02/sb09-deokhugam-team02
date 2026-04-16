package com.deokhugam.deokhugam_server.domain.book.mapper;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookDto toDto(Book book, int reviewCount, double rating) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getIsbn(),
                book.getThumbnailUrl(),
                reviewCount,
                rating,
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}