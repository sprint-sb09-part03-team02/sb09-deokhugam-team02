package com.deokhugam.deokhugam_server.domain.user.batch;

import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.user.service.PowerUserService;
import com.deokhugam.deokhugam_server.global.type.Period;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PowerUserBatchTest {

  @InjectMocks
  private PowerUserBatch powerUserBatch;

  @Mock
  private PowerUserService powerUserService;

  @Test
  @DisplayName("성공: 데일리 배치 실행 시 파워 유저 서비스의 산출 로직을 호출한다")
  void executeDaily_Success() {
    // when
    powerUserBatch.executeDaily();
    verify(powerUserService).calculateAndSavePowerUserRanks(Period.DAILY);
  }
}
