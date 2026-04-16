package com.deokhugam.deokhugam_server.domain.book.repository;

import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;

@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom{
  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Book> searchBooks(BookSearchRequest request) {
    return null;
  }

  @Override
  public List<Book> findPopularBooks(LocalDateTime startDate, LocalDateTime endDate, int limit) {
    return List.of();
  }

  @Override
  public List<BookRankQueryDto> findBookStatisticsForRanking(LocalDate startDate,
      LocalDate endDate) {
    return queryFactory
        .select(Projections.constructor(BookRankQueryDto.class,
            review.book.id,
            review.count(),
            review.rating.avg()
            ))
        .from(review)
        .where(review.createdAt.between(
            startDate.atStartOfDay(),
            endDate.atTime(LocalTime.MAX)
        ))
        .groupBy(review.book.id)
        .fetch();
  }
}
