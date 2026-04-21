package com.deokhugam.deokhugam_server.domain.review.event; // 패키지 경로 꼭 확인!

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewLikeRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.review.service.ReviewServiceImpl;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class ReviewLikedEventTest {

  @InjectMocks
  private ReviewServiceImpl reviewService;

  @Mock private ReviewRepository reviewRepository;
  @Mock private ReviewLikeRepository reviewLikeRepository;
  @Mock private UserRepository userRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @Test
  @DisplayName("성공: 좋아요를 누르면 ReviewLikedEvent가 발행된다")
  void should_PublishReviewLikedEvent() {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    User author = User.builder().id(UUID.randomUUID()).build();
    Review review = Review.builder().id(reviewId).user(author).content("내용").build();

    given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(userId)).willReturn(Optional.of(User.builder().id(userId).build()));
    given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(Optional.empty());

    // when
    reviewService.likeReview(reviewId, userId);

    // then
    verify(eventPublisher).publishEvent(any(ReviewLikedEvent.class));
  }
}