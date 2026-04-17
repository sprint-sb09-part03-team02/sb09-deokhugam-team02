package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import java.time.LocalDate;
import java.util.List;

public interface BookRepositoryCustom {

    List<Book> searchBooks(BookSearchRequest request);

    long countBooks(BookSearchRequest request);

    List<BookRankQueryDto> findBookStatisticsForRanking(LocalDate startDate, LocalDate endDate);
}