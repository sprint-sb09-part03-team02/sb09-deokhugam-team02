package com.deokhugam.deokhugam_server.domain.notification.repository;

import com.deokhugam.deokhugam_server.domain.notification.entity.QNotification.notification;

import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationSearchRequest;
import com.deokhugam.deokhugam_server.domain.notification.entity.Notification;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> searchNotifications(NotificationSearchRequest request) {
        return queryFactory
            .selectFrom(notification)
            .where(
                notification.userId.eq(request.getUserId()),
                notification.isDeleted.isFalse(),
                cursorCondition(request.getAfter(), request.getCursor(), request.getDirection())
            )
            .orderBy(orderSpecifier(request.getDirection()))
            .limit(request.getLimit() + 1)
            .fetch();
    }

    @Override
    public long countByUserId(UUID userId) {
        Long count = queryFactory
            .select(notification.count())
            .from(notification)
            .where(
                notification.userId.eq(userId),
                notification.isDeleted.isFalse()
            )
            .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression cursorCondition(LocalDateTime after, String cursor, String direction) {
        if (after == null || cursor == null) return null;
        UUID cursorId = UUID.fromString(cursor);
        boolean isDesc = !"ASC".equalsIgnoreCase(direction);
        if (isDesc) {
            return notification.createdAt.lt(after)
                .or(notification.createdAt.eq(after).and(notification.id.lt(cursorId)));
        } else {
            return notification.createdAt.gt(after)
                .or(notification.createdAt.eq(after).and(notification.id.gt(cursorId)));
        }
    }

    private OrderSpecifier<LocalDateTime> orderSpecifier(String direction) {
        return "ASC".equalsIgnoreCase(direction)
            ? notification.createdAt.asc()
            : notification.createdAt.desc();
    }
}
