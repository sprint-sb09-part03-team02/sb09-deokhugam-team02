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
            comment.isDeleted.isFalse() // 목록 조회 시 삭제된 데이터 제외
        )
        // 명세서 기반 정렬 조건 적용
        .orderBy(
            getOrderSpecifier(request.getDirection()),
            comment.id.desc() // 동시간대 데이터 정렬을 위한 ID 보조 정렬
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
            comment.isDeleted.isFalse() // 일반 카운트 시 삭제 데이터 제외
        )
        .fetchOne();
  }

  private BooleanExpression eqReviewId(UUID reviewId) {
    return reviewId != null ? comment.reviewId.eq(reviewId) : null;
  }

  private BooleanExpression eqUserId(UUID userId) {
    return userId != null ? comment.userId.eq(userId) : null;
  }

  // 복합 커서 페이지네이션 로직 (createdAt + ID)
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