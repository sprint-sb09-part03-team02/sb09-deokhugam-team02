package com.deokhugam.deokhugam_server.global.scheduler;

import com.deokhugam.deokhugam_server.global.type.Period;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingScheduler {

  private final JobLauncher jobLauncher;

  @Qualifier("rankingJob")
  private final Job rankingJob;

  // 1. 매일 새벽 3시: DAILY와 ALL_TIME을 함께 갱신 (홈 화면 데이터 보장)
  @Scheduled(cron = "0 0 3 * * *")
  public void runDailyAndAllTimeRanking() {
    log.info("[Batch] Starting Daily and All-Time ranking jobs...");
    runJob(Period.DAILY);
    runJob(Period.ALL_TIME);
  }

  // 2. 매주 월요일 새벽 4시: WEEKLY 갱신
  @Scheduled(cron = "0 0 4 * * MON")
  public void runWeeklyRanking() {
    log.info("[Batch] Starting Weekly ranking job...");
    runJob(Period.WEEKLY);
  }

  // 3. 매달 1일 새벽 5시: MONTHLY 갱신
  @Scheduled(cron = "0 0 5 1 * *")
  public void runMonthlyRanking() {
    log.info("[Batch] Starting Monthly ranking job...");
    runJob(Period.MONTHLY);
  }

  /**
   * 공통 Job 실행 메서드
   */
  private void runJob(Period period) {
    try {
      jobLauncher.run(rankingJob, new JobParametersBuilder()
        .addString("period", period.name())
        .addLong("requestedAt", System.currentTimeMillis())
        .toJobParameters());
    } catch (Exception e) {
      log.error("[Batch] {} ranking job launch failed", period.name(), e);
    }
  }
}
