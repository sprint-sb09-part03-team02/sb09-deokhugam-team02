package com.deokhugam.deokhugam_server.domain.review.service;

import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.mapper.ReviewMapper;
import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularReviewService {

  private final ReviewRepository reviewRepository;
  private final PopularReviewRepository popularReviewRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final ReviewMapper reviewMapper;

  @Transactional
  public void calculateAndSaveReviewRanks(Period periodType) {
    LocalDate endDate = LocalDate.now().minusDays(1);
    LocalDate startDate = getStartDate(periodType, endDate);

    List<ReviewRankQueryDto> statistics = reviewRepository.findReviewStatistics(startDate, endDate);

    List<PopularReview> rankings = statistics.stream()
        .map(stat -> PopularReview.builder()
            .review(reviewRepository.getReferenceById(stat.reviewId()))
            .periodType(periodType)
            .score(stat.calculateScore())
            .likeCount(stat.likeCount())
            .commentCount(stat.commentCount())
            .calculatedDate(endDate)
            .build())
        .sorted(Comparator.comparing(PopularReview::getScore).reversed())
        .toList();

    for (int i = 0; i < rankings.size(); i++) {
      rankings.get(i).assignRankOrder(i + 1);
    }

    List<PopularReview> existingRankings =
        popularReviewRepository.findAllByPeriodTypeAndCalculatedDate(periodType, endDate);
    if (!existingRankings.isEmpty()) {
      popularReviewRepository.deleteAllInBatch(existingRankings);
    }
    popularReviewRepository.saveAll(rankings);
  }

  public List<PopularReviewDto> getPopularReviews(Period periodType, LocalDate date) {
    return popularReviewRepository.findAllWithFetchJoin(periodType, date)
        .stream()
        .map(reviewMapper::toPopularDto)
        .toList();
  }

  private LocalDate getStartDate(Period type, LocalDate endDate) {
    return switch (type) {
      case DAILY -> endDate;
      case WEEKLY -> endDate.minusWeeks(1);
      case MONTHLY -> endDate.minusMonths(1);
      case ALL_TIME -> LocalDate.of(2000, 1, 1);
    };
  }
}