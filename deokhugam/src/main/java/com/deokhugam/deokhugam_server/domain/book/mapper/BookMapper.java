package com.deokhugam.deokhugam_server.domain.book.mapper;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
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

  public BookDto toDto(BookSearchQueryDto queryDto) {
    return new BookDto(
            queryDto.id(),
            queryDto.title(),
            queryDto.author(),
            queryDto.description(),
            queryDto.publisher(),
            queryDto.publishedDate(),
            queryDto.isbn(),
            queryDto.thumbnailUrl(),
            (int) queryDto.reviewCount(),
            queryDto.rating(),
            queryDto.createdAt(),
            queryDto.updatedAt()
    );
  }

  public PopularBookDto toPopularDto(PopularBook popularBook) {
    if (popularBook == null) {
      return null;
    }

    Book book = popularBook.getBook();

    return new PopularBookDto(
            popularBook.getId(),
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getThumbnailUrl(),
            popularBook.getPeriodType(),
            popularBook.getRankOrder(),
            popularBook.getScore(),
            popularBook.getReviewCount() == null ? 0 : popularBook.getReviewCount().intValue(),
            popularBook.getRating() == null ? 0.0 : popularBook.getRating(),
            popularBook.getCreatedAt()
    );
  }
}