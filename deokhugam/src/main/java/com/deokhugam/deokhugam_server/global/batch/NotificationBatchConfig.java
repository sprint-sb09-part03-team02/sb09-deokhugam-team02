package com.deokhugam.deokhugam_server.global.batch;

import com.deokhugam.deokhugam_server.domain.notification.batch.NotificationCleanupBatch;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class NotificationBatchConfig {

  private final NotificationCleanupBatch notificationCleanupBatch;
  private final BatchMetricsListener batchMetricsListener;

  @Bean
  public Job notificationCleanupJob(
      JobRepository jobRepository,
      Step notificationCleanupStep
  ) {
    return new JobBuilder("notificationCleanupJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .listener(batchMetricsListener)
        .start(notificationCleanupStep)
        .build();
  }

  @Bean
  public Step notificationCleanupStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager
  ) {
    return new StepBuilder("notificationCleanupStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          notificationCleanupBatch.execute();
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }
}
