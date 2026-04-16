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
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.LocalDateTime;
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
        // 객체 참조 방식이므로 fetchJoin을 써서 User와 Book 정보를 한 번에 가져오면 성능 최적화(N+1 방지) 가능!
        .leftJoin(review.book).fetchJoin()
        .leftJoin(review.user).fetchJoin()
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
        // 객체 참조 방식으로 Join 조건 수정 (review_id 대신 review 객체 비교)
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

  // 이제 review.userId가 아니라 review.user.id로 접근
  private BooleanExpression eqUserId(UUID userId) {
    return userId != null ? review.user.id.eq(userId) : null;
  }

  // 이제 review.bookId가 아니라 review.book.id로 접근
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