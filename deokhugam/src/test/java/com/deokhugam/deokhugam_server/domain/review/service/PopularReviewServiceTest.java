package com.deokhugam.deokhugam_server.domain.review.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewRankedEvent;
import com.deokhugam.deokhugam_server.domain.review.mapper.ReviewMapper;
import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);

    popularReviewService = new PopularReviewService(
        reviewRepository,
        popularReviewRepository,
        bookRepository,
        userRepository,
        reviewMapper,
        eventPublisher
    );
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
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ReviewRankQueryDto stat = new ReviewRankQueryDto(reviewId, 10L, 5L);

    when(reviewRepository.findReviewStatistics(any(), any()))
        .thenReturn(List.of(stat));

    Review mockReview = mock(Review.class);
    User mockUser = mock(User.class);

    when(reviewRepository.getReferenceById(reviewId)).thenReturn(mockReview);
    when(mockReview.getUser()).thenReturn(mockUser);
    when(mockReview.getId()).thenReturn(reviewId);
    when(mockReview.getContent()).thenReturn("테스트 콘텐츠");
    when(mockUser.getId()).thenReturn(userId);

    when(popularReviewRepository.findAllByPeriodTypeAndCalculatedDate(any(), any()))
        .thenReturn(Collections.emptyList());

    // when
    popularReviewService.calculateAndSaveReviewRanks(period);

    // then
    verify(popularReviewRepository).saveAll(anyList());
    verify(eventPublisher).publishEvent(any(ReviewRankedEvent.class));
  }
}