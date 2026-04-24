package com.deokhugam.deokhugam_server.domain.comment.repository;

import static com.deokhugam.deokhugam_server.domain.comment.entity.QComment.comment;
import static com.deokhugam.deokhugam_server.domain.user.entity.QUser.user;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
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
  public List<CommentDto> searchComments(CommentSearchRequest request) {
    int pageSize = request.getLimit();

    return queryFactory
        .select(Projections.constructor(CommentDto.class,
            comment.id,
            comment.review.id,
            comment.userId,
            user.nickname,
            comment.content,
            comment.createdAt,
            comment.updatedAt
        ))
        .from(comment)
        .leftJoin(user).on(comment.userId.eq(user.id))
        .where(
            eqReviewId(request.getReviewId()),
            eqUserId(request.getUserId()),
            ltCursorAfter(request.getAfter(), request.getCursor()),
            comment.isDeleted.isFalse()
        )
        .orderBy(
            getOrderSpecifier(request.getDirection()),
            comment.id.desc()
        )
        .limit(pageSize + 1)
        .fetch();
  }

  @Override
  public long countComments(UUID reviewId) {
    return queryFactory
        .select(comment.count())
        .from(comment)
        .where(
            eqReviewId(reviewId),
            comment.isDeleted.isFalse()
        )
        .fetchOne();
  }

  private BooleanExpression eqReviewId(UUID reviewId) {
    return reviewId != null ? comment.review.id.eq(reviewId) : null;
  }

  private BooleanExpression eqUserId(UUID userId) {
    return userId != null ? comment.userId.eq(userId) : null;
  }

  private BooleanExpression ltCursorAfter(LocalDateTime after, String cursor) {
    if (after == null || cursor == null) return null;

    return comment.createdAt.lt(after)
        .or(comment.createdAt.eq(after)
            .and(comment.id.lt(UUID.fromString(cursor))));
  }

  private OrderSpecifier<LocalDateTime> getOrderSpecifier(String direction) {
    return "ASC".equalsIgnoreCase(direction) ? comment.createdAt.asc() : comment.createdAt.desc();
  }
}
