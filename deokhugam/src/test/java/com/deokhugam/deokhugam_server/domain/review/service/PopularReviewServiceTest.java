package com.deokhugam.deokhugam_server.domain.review.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewRankedEvent;
import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

class PopularReviewServiceTest {

  @Mock private ReviewRepository reviewRepository;
  @Mock private PopularReviewRepository popularReviewRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private PopularReviewService popularReviewService;

  @Test
  @DisplayName("인기 리뷰 선정 성공")
  void calculateAndSaveReviewRanks_Success() {
    Period period = Period.DAILY;
    LocalDate endDate = LocalDate.now().minusDays(1);
    LocalDate startDate = endDate;

    ReviewRankQueryDto stat1 = new ReviewRankQueryDto(UUID.randomUUID(), 10L, 5L);
    ReviewRankQueryDto stat2 = new ReviewRankQueryDto(UUID.randomUUID(), 5L, 2L);

    when(reviewRepository.findReviewStatistics(any(), any()))
        .thenReturn(List.of(stat1, stat2));
    when(popularReviewRepository.findAllByPeriodTypeAndCalculatedDate(any(), any()))
        .thenReturn(Collections.emptyList());

    // when
    popularReviewService.calculateAndSaveReviewRanks(period);

    // then
    verify(popularReviewRepository, times(1)).saveAll(anyList());
    verify(eventPublisher, times(2)).publishEvent(any(ReviewRankedEvent.class));
  }
}

