package com.deokhugam.deokhugam_server.domain.book.mapper;

import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import org.springframework.stereotype.Component;

@Component
public class PopularBookMapper {

    public PopularBookDto toDto(PopularBook popularBook) {
        return new PopularBookDto(
                popularBook.getId(),
                popularBook.getBook().getId(),
                popularBook.getBook().getTitle(),
                popularBook.getBook().getAuthor(),
                popularBook.getBook().getThumbnailUrl(),
                popularBook.getPeriodType(),
                popularBook.getRankOrder(),
                popularBook.getScore(),
                popularBook.getReviewCount().intValue(),
                popularBook.getRating(),
                popularBook.getCreatedAt()
        );
    }
}