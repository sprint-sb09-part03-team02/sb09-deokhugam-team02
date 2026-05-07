package com.deokhugam.deokhugam_server.domain.notification.event;

import com.deokhugam.deokhugam_server.domain.comment.event.CommentCreatedEvent;
import com.deokhugam.deokhugam_server.domain.notification.entity.NotificationType;
import com.deokhugam.deokhugam_server.domain.notification.service.NotificationService;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewLikedEvent;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewRankedEvent;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ReviewRepository reviewRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        Review review = reviewRepository.findByIdAndIsDeletedFalse(event.reviewId())
            .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

        UUID reviewOwnerId = review.getUser().getId();
        if (reviewOwnerId.equals(event.commentAuthorId())) {
            return; // 자신의 리뷰에 자신이 댓글을 달면 알림 생략
        }

        notificationService.createNotification(
            event.reviewId(),
            reviewOwnerId,
            NotificationType.REVIEW_COMMENTED,
            String.format("%s님이 회원님의 리뷰에 댓글을 남겼습니다.", event.commentAuthorNickname())
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewLiked(ReviewLikedEvent event) {
        if (event.targetUserId().equals(event.likerId())) {
            return; // 자신의 리뷰에 자신이 좋아요하면 알림 생략
        }

        notificationService.createNotification(
            event.reviewId(),
            event.targetUserId(),
            NotificationType.REVIEW_LIKED,
            String.format("%s님이 회원님의 리뷰에 좋아요를 눌렀습니다.", event.likerNickname())
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewRanked(ReviewRankedEvent event) {
        String content = String.format(
            "회원님의 리뷰가 %s 기간 인기 리뷰 %d위에 선정되었습니다.",
            event.period().name(), event.rank()
        );

        notificationService.createNotification(
            event.reviewId(),
            event.userId(),
            NotificationType.REVIEW_RANKED,
            content
        );
    }
}
