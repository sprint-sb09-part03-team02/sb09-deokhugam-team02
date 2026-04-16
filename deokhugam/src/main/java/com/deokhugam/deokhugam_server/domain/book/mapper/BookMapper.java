package com.deokhugam.deokhugam_server.domain.book.mapper;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookDto toDto(Book book) {
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