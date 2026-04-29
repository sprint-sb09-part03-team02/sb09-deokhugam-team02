package com.deokhugam.deokhugam_server.domain.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserRankQueryDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.PowerUserRepository;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PowerUserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PowerUserRepository powerUserRepository;

  @InjectMocks
  private PowerUserService powerUserService;

  @Mock
  private PopularReviewRepository popularReviewRepository;

  @Captor
  private ArgumentCaptor<List<PowerUser>> listCaptor;

  @Test
  @DisplayName("파워 유저 계산 성공")
  void calculateAndSavePowerUserRanks_Success() {
    // given
    Period period = Period.MONTHLY;
    UUID user1Id = UUID.randomUUID();
    UUID user2Id = UUID.randomUUID();

    UserRankQueryDto user1Stat = new UserRankQueryDto(user1Id, 0L, 10L, 20L);
    UserRankQueryDto user2Stat = new UserRankQueryDto(user2Id, 0L, 50L, 100L);

    given(userRepository.findUserActivityStatistics(any(), any())).willReturn(List.of(user1Stat, user2Stat));

    given(userRepository.getReferenceById(user1Id)).willReturn(User.builder().id(user1Id).build());
    given(userRepository.getReferenceById(user2Id)).willReturn(User.builder().id(user2Id).build());

    // when
    powerUserService.calculateAndSavePowerUserRanks(period);

    // then
    verify(powerUserRepository, atLeastOnce()).findAllByPeriodTypeAndCalculatedDate(any(), any());

    verify(powerUserRepository).saveAll(listCaptor.capture());
    List<PowerUser> savedRankings = listCaptor.getValue();

    assertThat(savedRankings).hasSize(2);

    PowerUser firstPlace = savedRankings.stream().filter(r -> r.getUser().getId().equals(user2Id))
        .findFirst().orElseThrow();

    assertThat(firstPlace.getRankOrder()).isEqualTo(1);
    assertThat(firstPlace.getScore()).isEqualTo(40.0);
  }

  @Test
  @DisplayName("파워 유저 실패 - 활동 통계 없음")
  void calculateAndSavePowerUserRanks_EmptyStatistics() {
    // given
    Period period = Period.MONTHLY;
    given(userRepository.findUserActivityStatistics(any(), any())).willReturn(List.of());

    // when
    powerUserService.calculateAndSavePowerUserRanks(period);

    // then
    verify(powerUserRepository).saveAll(listCaptor.capture());
    assertThat(listCaptor.getValue()).isEmpty();
    verify(powerUserRepository, never()).deleteAll(any());
  }
}
