package com.deokhugam.deokhugam_server.domain.book.repository;

import static com.deokhugam.deokhugam_server.domain.book.entity.QBook.book;
import static com.deokhugam.deokhugam_server.domain.book.entity.QPopularBook.popularBook;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.entity.QPopularBook;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
                    buildWhereCursorCondition(request)
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
            .having(buildHavingCursorCondition(request, reviewCountExpression, ratingExpression))
            .orderBy(
                    buildPrimaryOrderSpecifier(request, reviewCountExpression, ratingExpression),
                    buildCreatedAtOrderSpecifier(request.direction())
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
  public BookSearchQueryDto findBookDetail(UUID bookId) {
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
                    book.id.eq(bookId),
                    book.isDeleted.isFalse()
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
            .fetchOne();
  }

  @Override
  public List<BookRankQueryDto> findBookStatisticsForRanking(LocalDate startDate, LocalDate endDate) {
    return queryFactory
            .select(Projections.constructor(
                    BookRankQueryDto.class,
                    review.book.id,
                    review.id.countDistinct(),
                    review.rating.avg().coalesce(0.0)
            ))
            .from(review)
            .where(
                    review.isDeleted.isFalse(),
                    review.book.isDeleted.isFalse(),
                    review.createdAt.between(
                            startDate.atStartOfDay(),
                            endDate.atTime(LocalTime.MAX)
                    )
            )
            .groupBy(review.book.id)
            .fetch();
  }

  @Override
  public List<PopularBook> findPopularBooksWithPaging(
          Period period,
          String direction,
          String cursor,
          String after,
          int limit
  ) {
    QPopularBook popularBookSub = new QPopularBook("popularBookSub");

    return queryFactory
            .selectFrom(popularBook)
            .join(popularBook.book, book).fetchJoin()
            .where(
                    popularBook.periodType.eq(period),
                    popularBook.calculatedDate.eq(
                            JPAExpressions
                                    .select(popularBookSub.calculatedDate.max())
                                    .from(popularBookSub)
                                    .where(popularBookSub.periodType.eq(period))
                    ),
                    buildPopularBookCursorCondition(direction, cursor, after)
            )
            .orderBy(
                    buildPopularBookRankOrderSpecifier(direction),
                    buildPopularBookIdOrderSpecifier(direction)
            )
            .limit(limit + 1L)
            .fetch();
  }

  private BooleanExpression containsKeyword(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return null;
    }

    String normalizedKeyword = keyword.trim();
    String normalizedIsbnKeyword = normalizedKeyword.replace("-", "");

    return book.title.containsIgnoreCase(normalizedKeyword)
            .or(book.author.containsIgnoreCase(normalizedKeyword))
            .or(book.isbn.containsIgnoreCase(normalizedIsbnKeyword));
  }

  private BooleanExpression buildWhereCursorCondition(BookSearchRequest request) {
    if (!StringUtils.hasText(request.cursor()) || request.after() == null) {
      return null;
    }

    String orderBy = normalizeOrderBy(request.orderBy());

    return switch (orderBy) {
      case "title" -> compareTitleCursor(request.cursor(), request.after(), request.direction());
      case "publisheddate" -> comparePublishedDateCursor(
              request.cursor(),
              request.after(),
              request.direction()
      );
      default -> null;
    };
  }

  private BooleanExpression buildHavingCursorCondition(
          BookSearchRequest request,
          NumberExpression<Long> reviewCountExpression,
          NumberExpression<Double> ratingExpression
  ) {
    if (!StringUtils.hasText(request.cursor()) || request.after() == null) {
      return null;
    }

    String orderBy = normalizeOrderBy(request.orderBy());

    return switch (orderBy) {
      case "rating" -> compareRatingCursor(
              request.cursor(),
              request.after(),
              request.direction(),
              ratingExpression
      );
      case "reviewcount" -> compareReviewCountCursor(
              request.cursor(),
              request.after(),
              request.direction(),
              reviewCountExpression
      );
      default -> null;
    };
  }

  private BooleanExpression compareTitleCursor(
          String cursor,
          LocalDateTime after,
          String direction
  ) {
    if (isAsc(direction)) {
      return book.title.gt(cursor)
              .or(book.title.eq(cursor).and(book.createdAt.gt(after)));
    }

    return book.title.lt(cursor)
            .or(book.title.eq(cursor).and(book.createdAt.lt(after)));
  }

  private BooleanExpression comparePublishedDateCursor(
          String cursor,
          LocalDateTime after,
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
          LocalDateTime after,
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
          LocalDateTime after,
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

  private BooleanExpression buildPopularBookCursorCondition(
          String direction,
          String cursor,
          String after
  ) {
    if (!StringUtils.hasText(cursor) || !StringUtils.hasText(after)) {
      return null;
    }

    int rankCursor = Integer.parseInt(cursor);
    UUID afterId = UUID.fromString(after);

    if (isAsc(direction)) {
      return popularBook.rankOrder.gt(rankCursor)
              .or(popularBook.rankOrder.eq(rankCursor).and(popularBook.id.gt(afterId)));
    }

    return popularBook.rankOrder.lt(rankCursor)
            .or(popularBook.rankOrder.eq(rankCursor).and(popularBook.id.lt(afterId)));
  }

  private OrderSpecifier<?> buildPrimaryOrderSpecifier(
          BookSearchRequest request,
          NumberExpression<Long> reviewCountExpression,
          NumberExpression<Double> ratingExpression
  ) {
    Order order = isAsc(request.direction()) ? Order.ASC : Order.DESC;
    String orderBy = normalizeOrderBy(request.orderBy());

    return switch (orderBy) {
      case "publisheddate" -> new OrderSpecifier<>(order, book.publishedDate);
      case "rating" -> new OrderSpecifier<>(order, ratingExpression);
      case "reviewcount" -> new OrderSpecifier<>(order, reviewCountExpression);
      case "title" -> new OrderSpecifier<>(order, book.title);
      default -> new OrderSpecifier<>(order, book.title);
    };
  }

  private OrderSpecifier<?> buildCreatedAtOrderSpecifier(String direction) {
    return isAsc(direction) ? book.createdAt.asc() : book.createdAt.desc();
  }

  private OrderSpecifier<?> buildPopularBookRankOrderSpecifier(String direction) {
    return isAsc(direction) ? popularBook.rankOrder.asc() : popularBook.rankOrder.desc();
  }

  private OrderSpecifier<?> buildPopularBookIdOrderSpecifier(String direction) {
    return isAsc(direction) ? popularBook.id.asc() : popularBook.id.desc();
  }

  private String normalizeOrderBy(String orderBy) {
    if (!StringUtils.hasText(orderBy)) {
      return "title";
    }
    return orderBy.trim().toLowerCase(Locale.ROOT);
  }

  private boolean isAsc(String direction) {
    return "ASC".equalsIgnoreCase(direction);
  }
}