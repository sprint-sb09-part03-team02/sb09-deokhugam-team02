package com.deokhugam.deokhugam_server.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

  private final JobLauncher jobLauncher;

  @Qualifier("notificationCleanupJob")
  private final Job notificationCleanupJob;

  @Scheduled(cron = "${deokhugam.batch.notification-cleanup.cron:0 30 3 * * *}", zone = "Asia/Seoul")
  public void runNotificationCleanup() {
    try {
      log.info("[Batch] Starting notification cleanup job...");
      jobLauncher.run(notificationCleanupJob, new JobParametersBuilder()
          .addLong("requestedAt", System.currentTimeMillis())
          .toJobParameters());
    } catch (Exception e) {
      log.error("[Batch] Notification cleanup job launch failed", e);
    }
  }
}
