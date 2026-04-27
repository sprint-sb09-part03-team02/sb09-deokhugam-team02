package com.deokhugam.deokhugam_server.domain.user.batch;

import com.deokhugam.deokhugam_server.domain.user.service.PowerUserService;
import com.deokhugam.deokhugam_server.global.type.Period;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PowerUserBatch {
  private final PowerUserService powerUserService;

  @Scheduled(cron = "0 0 3 * * *")
  public void executeDaily() {
    execute(Period.DAILY);
  }
  public void execute(Period period) {
    powerUserService.calculateAndSavePowerUserRanks(period);
  }
}
