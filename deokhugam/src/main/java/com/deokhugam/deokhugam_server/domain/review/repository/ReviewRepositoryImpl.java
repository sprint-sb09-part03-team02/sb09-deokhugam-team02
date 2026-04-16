package com.deokhugam.deokhugam_server.domain.review.repository;

import static com.deokhugam.deokhugam_server.domain.comment.entity.QComment.comment;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReviewLike.reviewLike;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Review> searchReviews(ReviewSearchRequest request) {
    int pageSize = request.getLimit();

    return queryFactory
        .selectFrom(review)
        .where(
            ltCursorAfter(request.getAfter(), request.getCursor()),
            eqUserId(request.getUserId()),
            eqBookId(request.getBookId()),
            containsKeyword(request.getKeyword()),
            review.isDeleted.isFalse()
                )
                .orderBy(getOrderSpecifier(request.getOrderBy(), request.getDirection()))
        .limit(pageSize + 1)
        .fetch();
  }

  @Override
  public List<ReviewRankQueryDto> findReviewStatistics(LocalDate start, LocalDate end) {

    return queryFactory
        .select(Projections.constructor(ReviewRankQueryDto.class,
            review.id,
            reviewLike.count(),
            comment.count()
        ))
        .from(review)
        .leftJoin(reviewLike).on(reviewLike.reviewId.eq(review.id))
        .leftJoin(comment).on(comment.reviewId.eq(review.id))
        .where(review.createdAt.between(start.atStartOfDay(), end.atTime(LocalTime.MAX)))
        .groupBy(review.id)
        .fetch();
  }

  private BooleanExpression ltCursorAfter(LocalDateTime after, String cursor) {
    if (after == null) return null;

    return review.createdAt.lt(after)
        .or(review.createdAt.eq(after)
            .and(review.id.lt(UUID.fromString(cursor))));
  }

  private BooleanExpression eqUserId(UUID userId) {
    return userId != null ? review.userId.eq(userId) : null;
  }

  private BooleanExpression eqBookId(UUID bookId) {
    return bookId != null ? review.bookId.eq(bookId) : null;
  }

  private BooleanExpression containsKeyword(String keyword) {
    return StringUtils.hasText(keyword)
        ? review.content.contains(keyword)
        : null;
  }

  private OrderSpecifier<?> getOrderSpecifier(String orderBy, String direction) {
    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    if ("rating".equals(orderBy)) {
      return isAsc ? review.rating.asc() : review.rating.desc();
    }

    return isAsc ? review.createdAt.asc() : review.createdAt.desc();
  }
}