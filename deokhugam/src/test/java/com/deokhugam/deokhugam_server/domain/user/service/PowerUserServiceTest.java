package com.deokhugam.deokhugam_server.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserRankQueryDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.PowerUserRepository;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PowerUserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PowerUserRepository powerUserRepository;

  @Mock
  private PopularReviewRepository popularReviewRepository;

  @InjectMocks
  private PowerUserService powerUserService;

  private User user;
  private PowerUser powerUser;

  @BeforeEach
  void setUp() {
  }

  @Test
  @DisplayName("파워 유저 산정 성공: 활동 점수에 따라 순위가 올바르게 계산되고 저장된다")
  void calculateAndSavePowerUserRanks_Success() {
    // given
    Period period = Period.MONTHLY;
    UUID user1Id = UUID.randomUUID();
    UUID user2Id = UUID.randomUUID();

    UserRankQueryDto user1Stat = new UserRankQueryDto(user1Id, 0.0, 10L, 20L);
    UserRankQueryDto user2Stat = new UserRankQueryDto(user2Id, 0.0, 50L, 100L);

    given(userRepository.findUserActivityStatistics(any(), any()))
        .willReturn(List.of(user1Stat, user2Stat));
    given(popularReviewRepository.sumScoreByUserIdAndPeriod(any(), any(), any()))
        .willReturn(Optional.of(0.0));

    // when
    powerUserService.calculateAndSavePowerUserRanks(period);

    // then
    verify(powerUserRepository).saveAll(argThat(rankings -> {
      List<PowerUser> list = StreamSupport.stream(rankings.spliterator(), false).toList();

      PowerUser firstPlace = list.stream()
          .filter(r -> r.getScore() > 0)
          .max(Comparator.comparing(PowerUser::getScore))
          .orElseThrow();

      return firstPlace.getRankOrder() == 1;
    }));
    verify(powerUserRepository, atLeastOnce()).findAllByPeriodTypeAndCalculatedDate(any(), any());
  }
  @Test
  @DisplayName("파워 유저 산정 예외: 활동 통계 데이터가 없으면 저장 로직이 호출되지 않는다 (또는 빈 리스트 저장)")
  void calculateAndSavePowerUserRanks_EmptyStatistics() {
    // given
    Period period = Period.MONTHLY;
    given(userRepository.findUserActivityStatistics(any(), any())).willReturn(List.of());

    // when
    powerUserService.calculateAndSavePowerUserRanks(period);

    // then
    verify(powerUserRepository).saveAll(argThat(rankings -> !rankings.iterator().hasNext()));
  }
}