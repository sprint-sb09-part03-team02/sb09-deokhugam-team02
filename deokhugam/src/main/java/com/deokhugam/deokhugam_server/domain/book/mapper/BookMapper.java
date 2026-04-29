package com.deokhugam.deokhugam_server.domain.book.mapper;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.global.mapper.StaticImagePathMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = StaticImagePathMapper.class)
public interface BookMapper {

  @Mapping(target = "id", source = "book.id")
  @Mapping(target = "title", source = "book.title")
  @Mapping(target = "author", source = "book.author")
  @Mapping(target = "description", source = "book.description")
  @Mapping(target = "publisher", source = "book.publisher")
  @Mapping(target = "publishedDate", source = "book.publishedDate")
  @Mapping(target = "isbn", source = "book.isbn")
  @Mapping(target = "thumbnailUrl", source = "book.thumbnailUrl", qualifiedByName = "normalizeStaticImagePath")
  @Mapping(target = "reviewCount", source = "reviewCount")
  @Mapping(target = "rating", source = "rating")
  @Mapping(target = "createdAt", source = "book.createdAt")
  @Mapping(target = "updatedAt", source = "book.updatedAt")
  BookDto toDto(Book book, int reviewCount, double rating);

  @Mapping(target = "thumbnailUrl", source = "queryDto.thumbnailUrl", qualifiedByName = "normalizeStaticImagePath")
  @Mapping(target = "reviewCount", expression = "java((int) queryDto.reviewCount())")
  BookDto toDto(BookSearchQueryDto queryDto);


  @Mapping(target = "id", source = "id")
  @Mapping(target = "bookId", source = "book.id")
  @Mapping(target = "title", source = "book.title")
  @Mapping(target = "author", source = "book.author")
  @Mapping(target = "thumbnailUrl", source = "book.thumbnailUrl", qualifiedByName = "normalizeStaticImagePath")
  @Mapping(target = "period", source = "periodType")
  @Mapping(target = "rank", source = "rankOrder")
  @Mapping(target = "score", source = "score")
  PopularBookDto toPopularDto(PopularBook popularBook);
}
