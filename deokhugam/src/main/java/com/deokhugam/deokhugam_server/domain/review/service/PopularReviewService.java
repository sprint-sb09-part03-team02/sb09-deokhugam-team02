package com.deokhugam.deokhugam_server.domain.review.service;

import static com.deokhugam.deokhugam_server.global.util.PeriodUtil.getStartDate;

import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewRankedEvent;
import com.deokhugam.deokhugam_server.domain.review.mapper.ReviewMapper;
import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularReviewService {

  private final ReviewRepository reviewRepository;
  private final PopularReviewRepository popularReviewRepository;
  private final ReviewMapper reviewMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void calculateAndSaveReviewRanks(Period periodType) {
    LocalDate endDate = LocalDate.now().minusDays(1);
    LocalDate startDate = getStartDate(periodType, endDate);

    List<ReviewRankQueryDto> statistics = reviewRepository.findReviewStatistics(startDate, endDate);

    Map<UUID, Review> reviewMap = reviewRepository
        .findAllById(statistics.stream().map(ReviewRankQueryDto::reviewId).toList())
        .stream()
        .collect(Collectors.toMap(Review::getId, review -> review));

    List<PopularReview> rankings = statistics.stream()
        .map(stat -> PopularReview.builder()
            .review(reviewMap.get(stat.reviewId()))
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

  public CursorPageResponse<PopularReviewDto> getPopularReviews(
    Period period, String direction, Integer cursor, LocalDateTime after, int limitSize) {

    Limit limitWithNext = Limit.of(limitSize + 1);

    List<PopularReview> results = "DESC".equalsIgnoreCase(direction)
      ? popularReviewRepository.findPopularReviewsDesc(period, cursor, after, limitWithNext)
      : popularReviewRepository.findPopularReviewsAsc(period, cursor, after, limitWithNext);

    boolean hasNext = results.size() > limitSize;
    List<PopularReview> pagedResults = hasNext
      ? results.subList(0, limitSize)
      : results;

    List<PopularReviewDto> content = pagedResults.stream()
      .map(reviewMapper::toPopularDto)
      .toList();

    String nextCursor = null;
    LocalDateTime nextAfter = null;
    if (!content.isEmpty()) {
      PopularReview lastItem = pagedResults.get(pagedResults.size() - 1);
      nextCursor = String.valueOf(lastItem.getRankOrder());
      nextAfter = lastItem.getCreatedAt();
    }

    long totalElements = popularReviewRepository.countByPeriodType(period);

    return new CursorPageResponse<>(
      content, nextCursor, nextAfter, content.size(), totalElements, hasNext);
  }
}
