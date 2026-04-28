package com.deokhugam.deokhugam_server.domain.notification.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.comment.event.CommentCreatedEvent;
import com.deokhugam.deokhugam_server.domain.notification.entity.NotificationType;
import com.deokhugam.deokhugam_server.domain.notification.service.NotificationService;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewLikedEvent;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewRankedEvent;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

  @InjectMocks
  private NotificationEventListener listener;

  @Mock
  private NotificationService notificationService;

  @Mock
  private ReviewRepository reviewRepository;

  @Test
  @DisplayName("성공: 다른 사용자가 댓글을 달면 리뷰 작성자에게 댓글 알림을 생성한다")
  void handleCommentCreated_CreateNotification() {
    UUID reviewId = UUID.randomUUID();
    UUID reviewOwnerId = UUID.randomUUID();
    UUID commentAuthorId = UUID.randomUUID();
    Review review = mockReview(reviewOwnerId);

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));

    listener.handleCommentCreated(new CommentCreatedEvent(reviewId, UUID.randomUUID(), commentAuthorId));

    verify(notificationService).createNotification(
      eq(reviewId),
      eq(reviewOwnerId),
      eq(NotificationType.REVIEW_COMMENTED),
      eq("회원님의 리뷰에 댓글이 달렸습니다.")
    );
  }

  @Test
  @DisplayName("성공: 자기 리뷰에 직접 댓글을 달면 알림을 생성하지 않는다")
  void handleCommentCreated_SkipSelfComment() {
    UUID reviewId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    Review review = mockReview(ownerId);
    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));

    listener.handleCommentCreated(new CommentCreatedEvent(reviewId, UUID.randomUUID(), ownerId));

    verify(notificationService, never()).createNotification(
      eq(reviewId),
      eq(ownerId),
      eq(NotificationType.REVIEW_COMMENTED),
      eq("회원님의 리뷰에 댓글이 달렸습니다.")
    );
  }

  @Test
  @DisplayName("실패: 댓글 이벤트의 리뷰가 없으면 예외가 발생한다")
  void handleCommentCreated_ReviewNotFound() {
    UUID reviewId = UUID.randomUUID();
    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());

    assertThatThrownBy(() ->
      listener.handleCommentCreated(new CommentCreatedEvent(reviewId, UUID.randomUUID(), UUID.randomUUID())))
      .isInstanceOf(DeokhugamException.class)
      .hasMessageContaining(ErrorCode.REVIEW_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("성공: 다른 사용자가 좋아요를 누르면 리뷰 작성자에게 좋아요 알림을 생성한다")
  void handleReviewLiked_CreateNotification() {
    UUID reviewId = UUID.randomUUID();
    UUID likerId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();

    listener.handleReviewLiked(new ReviewLikedEvent(reviewId, likerId, targetUserId, "리뷰 내용"));

    verify(notificationService).createNotification(
      eq(reviewId),
      eq(targetUserId),
      eq(NotificationType.REVIEW_LIKED),
      eq("회원님의 리뷰에 좋아요가 달렸습니다.")
    );
  }

  @Test
  @DisplayName("성공: 자기 리뷰에 직접 좋아요를 누르면 알림을 생성하지 않는다")
  void handleReviewLiked_SkipSelfLike() {
    UUID userId = UUID.randomUUID();

    listener.handleReviewLiked(new ReviewLikedEvent(UUID.randomUUID(), userId, userId, "리뷰 내용"));

    verify(notificationService, never()).createNotification(
      org.mockito.ArgumentMatchers.any(),
      org.mockito.ArgumentMatchers.any(),
      org.mockito.ArgumentMatchers.any(),
      org.mockito.ArgumentMatchers.any()
    );
  }

  @Test
  @DisplayName("성공: 인기 리뷰 진입 이벤트는 순위 알림을 생성한다")
  void handleReviewRanked_CreateNotification() {
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    listener.handleReviewRanked(new ReviewRankedEvent(reviewId, userId, Period.DAILY, 3, "리뷰 내용"));

    verify(notificationService).createNotification(
      eq(reviewId),
      eq(userId),
      eq(NotificationType.REVIEW_RANKED),
      eq("회원님의 리뷰가 DAILY 기간 인기 리뷰 3위에 선정되었습니다.")
    );
  }

  private Review mockReview(UUID userId) {
    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Review review = mock(Review.class);
    given(review.getUser()).willReturn(user);
    return review;
  }
}
