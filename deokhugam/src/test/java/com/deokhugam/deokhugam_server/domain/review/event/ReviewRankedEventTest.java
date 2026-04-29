package com.deokhugam.deokhugam_server.domain.review.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository; // 추가
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.review.service.PopularReviewService;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.Collections; // 추가
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class ReviewRankedEventTest {

  @InjectMocks
  private PopularReviewService popularReviewService;

  @Mock private ReviewRepository reviewRepository;

  @Mock private PopularReviewRepository popularReviewRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @Test
  @DisplayName("성공: 랭킹 진입 시 ReviewRankedEvent가 발행된다")
  void should_PublishReviewRankedEvent() {
    // [Given]
    UUID reviewId = UUID.randomUUID();
    ReviewRankQueryDto topReview = new ReviewRankQueryDto(reviewId, 10L, 5L);

    // 1. 통계 데이터 반환 설정
    given(reviewRepository.findReviewStatistics(any(), any())).willReturn(List.of(topReview));

    // 2. 기존 랭킹 삭제 로직을 지나가기 위해 빈 리스트 반환 설정 (NPE 방지 핵심)
    given(popularReviewRepository.findAllByPeriodTypeAndCalculatedDate(any(), any()))
        .willReturn(Collections.emptyList());

    Review mockReview = Review.builder()
        .id(reviewId)
        .user(User.builder().id(UUID.randomUUID()).build())
        .content("인기리뷰")
        .build();
    given(reviewRepository.findAllById(any())).willReturn(List.of(mockReview));

    // [When]
    popularReviewService.calculateAndSaveReviewRanks(Period.DAILY);

    // [Then]
    ArgumentCaptor<ReviewRankedEvent> captor = ArgumentCaptor.forClass(ReviewRankedEvent.class);
    verify(eventPublisher, atLeastOnce()).publishEvent(captor.capture());

    assertThat(captor.getValue().rank()).isEqualTo(1);
    assertThat(captor.getValue().reviewContent()).isEqualTo("인기리뷰");
  }
}
