package com.deokhugam.deokhugam_server.domain.comment.repository;

import static com.deokhugam.deokhugam_server.domain.comment.entity.QComment.comment;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Comment> searchComments(CommentSearchRequest request) {
    int pageSize = request.getLimit();

    return queryFactory
        .selectFrom(comment)
        .where(
            eqReviewId(request.getReviewId()),
            eqUserId(request.getUserId()),
            ltCursorAfter(request.getAfter(), request.getCursor()),
            comment.isDeleted.isFalse()
        )
        .orderBy(getOrderSpecifier(request.getOrderBy(), request.getDirection()))
        .limit(pageSize + 1)
        .fetch();
  }

  private BooleanExpression eqReviewId(UUID reviewId) {
    return reviewId != null ? comment.reviewId.eq(reviewId) : null;
  }

  private BooleanExpression eqUserId(UUID userId) {
    return userId != null ? comment.userId.eq(userId) : null;
  }

  private BooleanExpression ltCursorAfter(LocalDateTime after, String cursor) {
    if (after == null) return null;

    return comment.createdAt.lt(after)
        .or(comment.createdAt.eq(after)
            .and(comment.id.lt(UUID.fromString(cursor))));
  }

  private OrderSpecifier<?> getOrderSpecifier(String orderBy, String direction) {
    boolean isAsc = "ASC".equalsIgnoreCase(direction);
    return isAsc ? comment.createdAt.asc() : comment.createdAt.desc();
  }
}