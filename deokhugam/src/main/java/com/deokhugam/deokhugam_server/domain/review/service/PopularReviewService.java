package com.deokhugam.deokhugam_server.domain.review.service;

import static com.deokhugam.deokhugam_server.global.util.PeriodUtil.getStartDate;

import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewRankedEvent;
import com.deokhugam.deokhugam_server.domain.review.mapper.ReviewMapper;
import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher eventPublisher;

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

    rankings.stream()
        .filter(r -> r.getRankOrder() <= 10)
        .forEach(r -> eventPublisher.publishEvent(new ReviewRankedEvent(
            r.getReview().getId(),
            r.getReview().getUser().getId(),
            periodType,
            r.getRankOrder(),
            r.getReview().getContent()
        )));
  }

  public List<PopularReviewDto> getPopularReviews(Period periodType, LocalDate date) {
    return popularReviewRepository.findAllByPeriodTypeAndCalculatedDate(periodType, date)
        .stream()
        .map(reviewMapper::toPopularDto)
        .toList();
  }

}