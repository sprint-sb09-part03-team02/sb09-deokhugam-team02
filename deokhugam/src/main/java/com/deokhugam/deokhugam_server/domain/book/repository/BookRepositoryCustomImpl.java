package com.deokhugam.deokhugam_server.domain.book.repository;

import static com.deokhugam.deokhugam_server.domain.book.entity.QBook.book;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<BookSearchQueryDto> searchBooks(BookSearchRequest request) {
    NumberExpression<Long> reviewCountExpression = review.id.countDistinct();
    NumberExpression<Double> ratingExpression = review.rating.avg().coalesce(0.0);

    return queryFactory
            .select(Projections.constructor(
                    BookSearchQueryDto.class,
                    book.id,
                    book.title,
                    book.author,
                    book.description,
                    book.publisher,
                    book.publishedDate,
                    book.isbn,
                    book.thumbnailUrl,
                    reviewCountExpression,
                    ratingExpression,
                    book.createdAt,
                    book.updatedAt
            ))
            .from(book)
            .leftJoin(review).on(
                    review.book.eq(book),
                    review.isDeleted.isFalse()
            )
            .where(
                    book.isDeleted.isFalse(),
                    containsKeyword(request.keyword()),
                    applyCursorCondition(request, reviewCountExpression, ratingExpression)
            )
            .groupBy(
                    book.id,
                    book.title,
                    book.author,
                    book.description,
                    book.publisher,
                    book.publishedDate,
                    book.isbn,
                    book.thumbnailUrl,
                    book.createdAt,
                    book.updatedAt
            )
            .orderBy(
                    getPrimaryOrderSpecifier(request.orderBy(), request.direction(), reviewCountExpression, ratingExpression),
                    getCreatedAtOrderSpecifier(request.direction())
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
                    review.book.id,
                    review.id.countDistinct(),
                    review.rating.avg()
            ))
            .from(review)
            .where(
                    review.createdAt.between(
                            startDate.atStartOfDay(),
                            endDate.atTime(LocalTime.MAX)
                    )
            )
            .groupBy(review.book.id)
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

  private BooleanExpression applyCursorCondition(
          BookSearchRequest request,
          NumberExpression<Long> reviewCountExpression,
          NumberExpression<Double> ratingExpression
  ) {
    if (!StringUtils.hasText(request.cursor()) || request.after() == null) {
      return null;
    }

    String orderBy = request.orderBy();
    String direction = request.direction();

    return switch (orderBy) {
      case "publishedDate" -> comparePublishedDateCursor(request.cursor(), request.after(), direction);
      case "rating" -> compareRatingCursor(request.cursor(), request.after(), direction, ratingExpression);
      case "reviewCount" -> compareReviewCountCursor(request.cursor(), request.after(), direction, reviewCountExpression);
      case "title" -> compareTitleCursor(request.cursor(), request.after(), direction);
      default -> compareTitleCursor(request.cursor(), request.after(), direction);
    };
  }

  private BooleanExpression compareTitleCursor(String cursor, java.time.LocalDateTime after, String direction) {
    if (isAsc(direction)) {
      return book.title.gt(cursor)
              .or(book.title.eq(cursor).and(book.createdAt.gt(after)));
    }

    return book.title.lt(cursor)
            .or(book.title.eq(cursor).and(book.createdAt.lt(after)));
  }

  private BooleanExpression comparePublishedDateCursor(
          String cursor,
          java.time.LocalDateTime after,
          String direction
  ) {
    LocalDate publishedDateCursor = LocalDate.parse(cursor);

    if (isAsc(direction)) {
      return book.publishedDate.gt(publishedDateCursor)
              .or(book.publishedDate.eq(publishedDateCursor).and(book.createdAt.gt(after)));
    }

    return book.publishedDate.lt(publishedDateCursor)
            .or(book.publishedDate.eq(publishedDateCursor).and(book.createdAt.lt(after)));
  }

  private BooleanExpression compareRatingCursor(
          String cursor,
          java.time.LocalDateTime after,
          String direction,
          NumberExpression<Double> ratingExpression
  ) {
    double ratingCursor = Double.parseDouble(cursor);

    if (isAsc(direction)) {
      return ratingExpression.gt(ratingCursor)
              .or(ratingExpression.eq(ratingCursor).and(book.createdAt.gt(after)));
    }

    return ratingExpression.lt(ratingCursor)
            .or(ratingExpression.eq(ratingCursor).and(book.createdAt.lt(after)));
  }

  private BooleanExpression compareReviewCountCursor(
          String cursor,
          java.time.LocalDateTime after,
          String direction,
          NumberExpression<Long> reviewCountExpression
  ) {
    long reviewCountCursor = Long.parseLong(cursor);

    if (isAsc(direction)) {
      return reviewCountExpression.gt(reviewCountCursor)
              .or(reviewCountExpression.eq(reviewCountCursor).and(book.createdAt.gt(after)));
    }

    return reviewCountExpression.lt(reviewCountCursor)
            .or(reviewCountExpression.eq(reviewCountCursor).and(book.createdAt.lt(after)));
  }

  private OrderSpecifier<?> getPrimaryOrderSpecifier(
          String orderBy,
          String direction,
          NumberExpression<Long> reviewCountExpression,
          NumberExpression<Double> ratingExpression
  ) {
    Order order = isAsc(direction) ? Order.ASC : Order.DESC;

    return switch (orderBy) {
      case "publishedDate" -> new OrderSpecifier<>(order, book.publishedDate);
      case "rating" -> new OrderSpecifier<>(order, ratingExpression);
      case "reviewCount" -> new OrderSpecifier<>(order, reviewCountExpression);
      case "title" -> new OrderSpecifier<>(order, book.title);
      default -> new OrderSpecifier<>(order, book.title);
    };
  }

  private OrderSpecifier<?> getCreatedAtOrderSpecifier(String direction) {
    return isAsc(direction) ? book.createdAt.asc() : book.createdAt.desc();
  }

  private boolean isAsc(String direction) {
    return "ASC".equalsIgnoreCase(direction);
  }
}