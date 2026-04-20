package com.deokhugam.deokhugam_server.domain.review.repository;

import static com.deokhugam.deokhugam_server.domain.comment.entity.QComment.comment;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReviewLike.reviewLike;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<ReviewDto> searchReviews(ReviewSearchRequest request) {
    int pageSize = request.getLimit();

    return queryFactory
        .select(Projections.constructor(ReviewDto.class,
            review.id,
            review.book.id,
            review.book.title,
            review.book.thumbnailUrl,
            review.user.id,
            review.user.nickname,
            review.content,
            review.rating,
            review.likeCount,
            review.commentCount,
            reviewLike.id.isNotNull(), // 한 번의 쿼리로 좋아요 여부 확인 (N+1 해결)
            review.createdAt,
            review.updatedAt
        ))
        .from(review)
        .leftJoin(review.book)
        .leftJoin(review.user)
        .leftJoin(reviewLike).on(reviewLike.review.eq(review).and(reviewLike.user.id.eq(request.getRequestUserId())))
        .where(
            ltCursorAfter(request.getAfter(), request.getCursor()),
            eqUserId(request.getUserId()),
            eqBookId(request.getBookId()),
            containsKeyword(request.getKeyword()),
            review.isDeleted.isFalse()
        )
        .orderBy(
            getOrderSpecifier(request.getOrderBy(), request.getDirection()),
            review.id.desc()
        )
        .limit(pageSize + 1)
        .fetch();
  }

  @Override
  public List<ReviewRankQueryDto> findReviewStatistics(LocalDate start, LocalDate end) {
    return queryFactory
        .select(Projections.constructor(ReviewRankQueryDto.class,
            review.id,
            reviewLike.id.countDistinct(),
            comment.id.countDistinct()
        ))
        .from(review)
        .leftJoin(reviewLike).on(reviewLike.review.eq(review))
        .leftJoin(comment).on(comment.reviewId.eq(review.id))
        .where(review.createdAt.between(start.atStartOfDay(), end.atTime(LocalTime.MAX)))
        .groupBy(review.id)
        .fetch();
  }

  private BooleanExpression ltCursorAfter(LocalDateTime after, String cursor) {
    if (after == null || cursor == null) return null;
    return review.createdAt.lt(after)
        .or(review.createdAt.eq(after)
            .and(review.id.lt(UUID.fromString(cursor))));
  }

  private BooleanExpression eqUserId(UUID userId) {
    return userId != null ? review.user.id.eq(userId) : null;
  }

  private BooleanExpression eqBookId(UUID bookId) {
    return bookId != null ? review.book.id.eq(bookId) : null;
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