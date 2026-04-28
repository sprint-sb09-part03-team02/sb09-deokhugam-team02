package com.deokhugam.deokhugam_server.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewRankedEvent;
import com.deokhugam.deokhugam_server.domain.review.mapper.ReviewMapper;
import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

class PopularReviewServiceTest {

  private PopularReviewService popularReviewService;
  private AutoCloseable closeable;

  @Mock private ReviewRepository reviewRepository;
  @Mock private PopularReviewRepository popularReviewRepository;
  @Mock private BookRepository bookRepository;
  @Mock private UserRepository userRepository;
  @Mock private ReviewMapper reviewMapper;
  @Mock private ApplicationEventPublisher eventPublisher;
  private Map<UUID, Review> reviewMap;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    popularReviewService = new PopularReviewService(
        reviewRepository, popularReviewRepository, bookRepository,
        userRepository, reviewMapper, eventPublisher
    );
    reviewMap = new HashMap<>();
  }

  @AfterEach
  void tearDown() throws Exception {
    closeable.close();
  }

  @Test
  @DisplayName("인기 리뷰 선정 성공")
  void calculateAndSaveReviewRanks_Success() {
    // given
    Period period = Period.DAILY;
    UUID reviewId1 = UUID.randomUUID();
    UUID reviewId2 = UUID.randomUUID();

    ReviewRankQueryDto highStat = new ReviewRankQueryDto(reviewId1, 10L, 5L);
    ReviewRankQueryDto lowStat = new ReviewRankQueryDto(reviewId2, 1L, 1L);

    when(reviewRepository.findReviewStatistics(any(), any()))
        .thenReturn(List.of(lowStat, highStat));

    setupMockReview(reviewId1, "높은 점수 리뷰");
    setupMockReview(reviewId2, "낮은 점수 리뷰");
    when(reviewRepository.findAllById(any())).thenReturn(List.copyOf(reviewMap.values()));

    when(popularReviewRepository.findAllByPeriodTypeAndCalculatedDate(any(), any()))
        .thenReturn(Collections.emptyList());

    // when
    popularReviewService.calculateAndSaveReviewRanks(period);

    // then
    ArgumentCaptor<List<PopularReview>> captor = ArgumentCaptor.forClass(List.class);
    verify(popularReviewRepository).saveAll(captor.capture());

    List<PopularReview> savedRankings = captor.getValue();
    assertThat(savedRankings).hasSize(2);
    assertThat(savedRankings.get(0).getRankOrder()).isEqualTo(1);
    assertThat(savedRankings.get(1).getRankOrder()).isEqualTo(2);

    verify(eventPublisher, times(2)).publishEvent(any(ReviewRankedEvent.class));
  }

  private void setupMockReview(UUID reviewId, String content) {
    Review mockReview = mock(Review.class);
    User mockUser = mock(User.class);

    when(mockReview.getId()).thenReturn(reviewId);
    when(mockReview.getUser()).thenReturn(mockUser);
    when(mockReview.getContent()).thenReturn(content);
    when(mockUser.getId()).thenReturn(UUID.randomUUID());
    reviewMap.put(reviewId, mockReview);
  }
}
