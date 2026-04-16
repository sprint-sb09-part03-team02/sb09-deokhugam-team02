package com.deokhugam.deokhugam_server.domain.book.mapper;

import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

  public static PopularBookDto toPopularDto(PopularBook entity) {
    return new PopularBookDto(
        entity.getId(),
        entity.getBook().getId(),
        entity.getBook().getTitle(),
        entity.getBook().getAuthor(),
        entity.getBook().getImageUrl(),
        entity.getPeriodType(),
        entity.getRankOrder(),
        entity.getScore(),
        entity.getReviewCount().intValue(),
        entity.getRating(),
        entity.getCreatedAt()
    );
  }
}