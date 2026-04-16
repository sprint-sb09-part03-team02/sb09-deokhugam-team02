package com.deokhugam.deokhugam_server.domain.book.repository;

import static com.deokhugam.deokhugam_server.domain.book.entity.QBook.book;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Book> searchBooks(BookSearchRequest request) {
    return queryFactory
            .select(book)
            .from(book)
            .leftJoin(review).on(
                    review.bookId.eq(book.id)
                            .and(review.isDeleted.isFalse())
            )
            .where(
                    book.isDeleted.isFalse(),
                    containsKeyword(request.keyword()),
                    applyCursorCondition(request)
            )
            .groupBy(book.id)
            .orderBy(
                    getPrimaryOrderSpecifier(request.orderBy(), request.direction()),
                    getCreatedAtOrderSpecifier(request.direction()),
                    getIdOrderSpecifier(request.direction())
            )
            .limit(request.limit() + 1L)
            .fetch();
  }

  @Override
  public long countBooks(BookSearchRequest request) {
    Long count = queryFactory
            .select(book.count())
            .from(book)
            .where(
                    book.isDeleted.isFalse(),
                    containsKeyword(request.keyword())
            )
            .fetchOne();

    return count == null ? 0L : count;
  }

  @Override
  public List<BookRankQueryDto> findBookStatisticsForRanking(LocalDate startDate, LocalDate endDate) {
    return queryFactory
            .select(Projections.constructor(
                    BookRankQueryDto.class,
                    review.bookId,
                    review.count(),
                    review.rating.avg()
            ))
            .from(review)
            .where(
                    review.createdAt.between(
                            startDate.atStartOfDay(),
                            endDate.atTime(LocalTime.MAX)
                    )
            )
            .groupBy(review.bookId)
            .fetch();
  }

  private BooleanExpression containsKeyword(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return null;
    }

    return book.title.containsIgnoreCase(keyword)
            .or(book.author.containsIgnoreCase(keyword))
            .or(book.isbn.containsIgnoreCase(keyword));
  }

  private BooleanExpression applyCursorCondition(BookSearchRequest request) {
    if (!StringUtils.hasText(request.cursor()) || request.after() == null) {
      return null;
    }

    String orderBy = request.orderBy();
    String direction = request.direction();

    return switch (orderBy) {
      case "publishedDate" -> publishedDateCursorCondition(request.cursor(), request.after(), direction);
      case "rating" -> ratingCursorCondition(request.cursor(), request.after(), direction);
      case "reviewCount" -> reviewCountCursorCondition(request.cursor(), request.after(), direction);
      case "title" -> titleCursorCondition(request.cursor(), request.after(), direction);
      default -> titleCursorCondition(request.cursor(), request.after(), direction);
    };
  }

  private BooleanExpression titleCursorCondition(String cursor, LocalDateTime after, String direction) {
    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    if (isAsc) {
      return book.title.gt(cursor)
              .or(book.title.eq(cursor).and(book.createdAt.gt(after)));
    }

    return book.title.lt(cursor)
            .or(book.title.eq(cursor).and(book.createdAt.lt(after)));
  }

  private BooleanExpression publishedDateCursorCondition(String cursor, LocalDateTime after, String direction) {
    LocalDate cursorDate = LocalDate.parse(cursor);
    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    if (isAsc) {
      return book.publishedDate.gt(cursorDate)
              .or(book.publishedDate.eq(cursorDate).and(book.createdAt.gt(after)));
    }

    return book.publishedDate.lt(cursorDate)
            .or(book.publishedDate.eq(cursorDate).and(book.createdAt.lt(after)));
  }

  private BooleanExpression ratingCursorCondition(String cursor, LocalDateTime after, String direction) {
    double cursorRating = Double.parseDouble(cursor);
    NumberExpression<Double> ratingExpression = review.rating.avg().coalesce(0.0);
    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    if (isAsc) {
      return ratingExpression.gt(cursorRating)
              .or(ratingExpression.eq(cursorRating).and(book.createdAt.gt(after)));
    }

    return ratingExpression.lt(cursorRating)
            .or(ratingExpression.eq(cursorRating).and(book.createdAt.lt(after)));
  }

  private BooleanExpression reviewCountCursorCondition(String cursor, LocalDateTime after, String direction) {
    long cursorCount = Long.parseLong(cursor);
    NumberExpression<Long> reviewCountExpression = review.count();
    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    if (isAsc) {
      return reviewCountExpression.gt(cursorCount)
              .or(reviewCountExpression.eq(cursorCount).and(book.createdAt.gt(after)));
    }

    return reviewCountExpression.lt(cursorCount)
            .or(reviewCountExpression.eq(cursorCount).and(book.createdAt.lt(after)));
  }

  private OrderSpecifier<?> getPrimaryOrderSpecifier(String orderBy, String direction) {
    Order order = "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC;

    return switch (orderBy) {
      case "publishedDate" -> new OrderSpecifier<>(order, book.publishedDate);
      case "rating" -> new OrderSpecifier<>(order, review.rating.avg().coalesce(0.0));
      case "reviewCount" -> new OrderSpecifier<>(order, review.count());
      case "title" -> new OrderSpecifier<>(order, book.title);
      default -> new OrderSpecifier<>(order, book.title);
    };
  }

  private OrderSpecifier<?> getCreatedAtOrderSpecifier(String direction) {
    Order order = "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC;
    return new OrderSpecifier<>(order, book.createdAt);
  }

  private OrderSpecifier<?> getIdOrderSpecifier(String direction) {
    Order order = "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC;
    return new OrderSpecifier<>(order, book.id);
  }
}