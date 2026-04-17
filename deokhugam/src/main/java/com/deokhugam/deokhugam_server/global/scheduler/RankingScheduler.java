package com.deokhugam.deokhugam_server.global.scheduler;

import com.deokhugam.deokhugam_server.domain.book.batch.PopularBookBatch;
import com.deokhugam.deokhugam_server.domain.review.batch.PopularReviewBatch;
import com.deokhugam.deokhugam_server.domain.user.batch.PowerUserBatch;
import com.deokhugam.deokhugam_server.global.type.Period;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingScheduler {

  private final PopularBookBatch popularBookBatch;
  private final PopularReviewBatch popularReviewBatch;
  private final PowerUserBatch powerUserBatch;

  @Scheduled(cron = "0 0 3 * * *")
  public void runDailyRanking() {
    log.info("[Batch] Daily ranking started");

    popularBookBatch.execute(Period.DAILY);
    popularReviewBatch.execute(Period.DAILY);

    powerUserBatch.execute(Period.DAILY);

  }
}