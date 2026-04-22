package com.deokhugam.deokhugam_server.domain.book.mapper;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

  @Mapping(target = "id", source = "book.id")
  @Mapping(target = "title", source = "book.title")
  @Mapping(target = "author", source = "book.author")
  @Mapping(target = "description", source = "book.description")
  @Mapping(target = "publisher", source = "book.publisher")
  @Mapping(target = "publishedDate", source = "book.publishedDate")
  @Mapping(target = "isbn", source = "book.isbn")
  @Mapping(target = "thumbnailUrl", source = "book.thumbnailUrl")
  @Mapping(target = "reviewCount", source = "reviewCount")
  @Mapping(target = "rating", source = "rating")
  @Mapping(target = "createdAt", source = "book.createdAt")
  @Mapping(target = "updatedAt", source = "book.updatedAt")
  BookDto toDto(Book book, int reviewCount, double rating);

  @Mapping(target = "reviewCount", expression = "java((int) queryDto.reviewCount())")
  BookDto toDto(BookSearchQueryDto queryDto);

  @Mapping(target = "id", source = "popularBook.id")
  @Mapping(target = "bookId", source = "popularBook.book.id")
  @Mapping(target = "title", source = "popularBook.book.title")
  @Mapping(target = "author", source = "popularBook.book.author")
  @Mapping(target = "thumbnailUrl", source = "popularBook.book.thumbnailUrl")
  @Mapping(target = "period", source = "popularBook.periodType")
  @Mapping(target = "rank", source = "popularBook.rankOrder")
  @Mapping(target = "score", source = "popularBook.score")
  @Mapping(
    target = "reviewCount",
    expression = "java(popularBook.getReviewCount() == null ? 0 : popularBook.getReviewCount().intValue())"
  )
  @Mapping(
    target = "rating",
    expression = "java(popularBook.getRating() == null ? 0.0 : popularBook.getRating())"
  )
  @Mapping(target = "createdAt", source = "popularBook.createdAt")
  PopularBookDto toPopularDto(PopularBook popularBook);
}
