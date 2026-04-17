package com.deokhugam.deokhugam_server.domain.book.mapper;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import org.springframework.stereotype.Component;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;

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

  public PopularBookDto toPopularDto(PopularBook popularBook) {
    if (popularBook == null)
      return null;
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
        popularBook.getReviewCount().intValue(),
        popularBook.getRating(),
        popularBook.getCreatedAt()
    );
  }
}
