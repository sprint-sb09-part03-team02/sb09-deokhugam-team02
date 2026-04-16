package com.deokhugam.deokhugam_server.domain.user.service;

import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserRankQueryDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.repository.PowerUserRepository;
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
public class PowerUserService {

  private final UserRepository userRepository;
  private final PowerUserRepository powerUserRepository;
  private final PopularReviewRepository popularReviewRepository;

  @Transactional
  public void calculateAndSavePowerUserRanks(Period periodType) {
    LocalDate endDate = LocalDate.now().minusDays(1);
    LocalDate startDate = getStartDate(periodType, endDate);

    List<UserRankQueryDto> statistics = userRepository.findUserActivityStatistics(startDate,
        endDate);

    List<PowerUser> rankings = statistics.stream()
        .map(stat -> {
          Double scoreSum = popularReviewRepository.sumScoreByUserIdAndPeriod(
              stat.userId(), periodType, endDate).orElse(0.0);

          return PowerUser.builder()
              .user(userRepository.getReferenceById(stat.userId()))
              .periodType(periodType)
              .reviewScoreSum(scoreSum)
              .likeCount(stat.givenLikeCount().intValue())
              .commentCount(stat.writtenCommentCount().intValue())
              .score((scoreSum * 0.5) + (stat.givenLikeCount() * 0.2) + (stat.writtenCommentCount()
                  * 0.3))
              .calculatedDate(endDate)
              .build();
        })
        .sorted(Comparator.comparing(PowerUser::getScore).reversed())
        .toList();

    for (int i = 0; i < rankings.size(); i++) {
      rankings.get(i).assignRankOrder(i + 1);
    }
    powerUserRepository.saveAll(rankings);
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