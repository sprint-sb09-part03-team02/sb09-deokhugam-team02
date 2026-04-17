package com.deokhugam.deokhugam_server.domain.book.mapper;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
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

    public PopularBookDto toDto(PopularBook entity) {
        return new PopularBookDto(
                entity.getId(),
                entity.getBook().getId(),
                entity.getBook().getTitle(),
                entity.getBook().getAuthor(),
                entity.getBook().getThumbnailUrl(),
                entity.getPeriodType(),
                entity.getRankOrder(),
                entity.getScore(),
                entity.getReviewCount() == null ? 0 : entity.getReviewCount().intValue(),
                entity.getRating() == null ? 0.0 : entity.getRating(),
                entity.getCreatedAt()
        );
    }
}