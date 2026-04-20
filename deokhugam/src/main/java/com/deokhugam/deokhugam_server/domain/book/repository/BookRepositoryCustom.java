package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookRepositoryCustom {

    List<BookSearchQueryDto> searchBooks(BookSearchRequest request);

    long countBooks(BookSearchRequest request);

    BookSearchQueryDto findBookDetail(UUID bookId);

    List<BookRankQueryDto> findBookStatisticsForRanking(LocalDate startDate, LocalDate endDate);

    List<PopularBook> findPopularBooksWithPaging(
            Period period,
            String direction,
            String cursor,
            String after,
            int limit
    );
}