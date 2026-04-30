package com.deokhugam.deokhugam_server.domain.review.repository;

import static com.deokhugam.deokhugam_server.domain.comment.entity.QComment.comment;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReview.review;
import static com.deokhugam.deokhugam_server.domain.review.entity.QReviewLike.reviewLike;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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

    List<OrderSpecifier<?>> orders = new ArrayList<>();
    boolean isAsc = "ASC".equalsIgnoreCase(request.getDirection());

    if ("rating".equals(request.getOrderBy())) {
      orders.add(isAsc ? review.rating.asc() : review.rating.desc());
      orders.add(isAsc ? review.createdAt.asc() : review.createdAt.desc()); // 보조 정렬
    } else {
      orders.add(isAsc ? review.createdAt.asc() : review.createdAt.desc()); // 기본 정렬
    }
    orders.add(review.id.desc());

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
            reviewLike.id.isNotNull(), // 좋아요 여부
            review.createdAt,
            review.updatedAt
        ))
        .from(review)
        .leftJoin(review.book)
        .leftJoin(review.user)
        .leftJoin(reviewLike).on(
            reviewLike.review.eq(review),
            eqRequestUserId(request.getRequestUserId())
        )
        .where(
            getCursorCondition(request.getAfter(), request.getCursor(), request.getDirection()),
            eqUserId(request.getUserId()),
            eqBookId(request.getBookId()),
            containsKeyword(request.getKeyword()),
            review.isDeleted.isFalse()
        )
        .orderBy(orders.toArray(new OrderSpecifier[0]))
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
        .leftJoin(reviewLike).on(reviewLike.review.eq(review)
          .and(reviewLike.createdAt.between(start.atStartOfDay(), end.atTime(LocalTime.MAX))))
        .leftJoin(comment).on(comment.review.eq(review)
          .and(comment.createdAt.between(start.atStartOfDay(), end.atTime(LocalTime.MAX))))
        .groupBy(review.id)
        .having(reviewLike.id.countDistinct().gt(0).or(comment.id.countDistinct().gt(0)))
        .fetch();
  }

  private BooleanExpression getCursorCondition(LocalDateTime after, String cursor, String direction) {
    if (after == null || cursor == null) return null;
    UUID uuidCursor = UUID.fromString(cursor);
    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    if (isAsc) {
      return review.createdAt.gt(after)
          .or(review.createdAt.eq(after).and(review.id.gt(uuidCursor)));
    } else {
      return review.createdAt.lt(after)
          .or(review.createdAt.eq(after).and(review.id.lt(uuidCursor)));
    }
  }

  private BooleanExpression eqRequestUserId(UUID requestUserId) {
    if (requestUserId == null) {
      return Expressions.asBoolean(true).isFalse();
    }
    return reviewLike.user.id.eq(requestUserId);
  }

  private BooleanExpression eqUserId(UUID userId) {
    return userId != null ? review.user.id.eq(userId) : null;
  }

  private BooleanExpression eqBookId(UUID bookId) {
    return bookId != null ? review.book.id.eq(bookId) : null;
  }

  private BooleanExpression containsKeyword(String keyword) {
    if (!StringUtils.hasText(keyword)) return null;
    return review.content.contains(keyword)
        .or(review.user.nickname.contains(keyword))
        .or(review.book.title.contains(keyword));
  }
}
