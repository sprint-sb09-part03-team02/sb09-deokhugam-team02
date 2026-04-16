package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Slice;

public interface BookRepositoryCustom {

    Slice<Book> searchBooks(BookSearchRequest request);

    List<Book> findPopularBooks(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int limit
    );
    List<BookRankQueryDto> findBookStatisticsForRanking(LocalDateTime startDate, LocalDateTime endDate);
}