package com.deokhugam.deokhugam_server.global.batch;

import com.deokhugam.deokhugam_server.domain.book.batch.PopularBookBatch;
import com.deokhugam.deokhugam_server.domain.review.batch.PopularReviewBatch;
import com.deokhugam.deokhugam_server.domain.user.batch.PowerUserBatch;
import com.deokhugam.deokhugam_server.global.type.Period;
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
public class RankingBatchConfig {

  private final PopularBookBatch popularBookBatch;
  private final PopularReviewBatch popularReviewBatch;
  private final PowerUserBatch powerUserBatch;
  private final BatchMetricsListener batchMetricsListener;

  @Bean
  public Job rankingJob(
      JobRepository jobRepository,
      Step popularBookRankingStep,
      Step popularReviewRankingStep,
      Step powerUserRankingStep
  ) {
    return new JobBuilder("rankingJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .listener(batchMetricsListener)
        .start(popularBookRankingStep)
        .next(popularReviewRankingStep)
        .next(powerUserRankingStep)
        .build();
  }

  @Bean
  public Step popularBookRankingStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager
  ) {
    return new StepBuilder("popularBookRankingStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          popularBookBatch.execute(resolvePeriod(chunkContext.getStepContext().getJobParameters().get("period")));
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }

  @Bean
  public Step popularReviewRankingStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager
  ) {
    return new StepBuilder("popularReviewRankingStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          popularReviewBatch.execute(resolvePeriod(chunkContext.getStepContext().getJobParameters().get("period")));
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }

  @Bean
  public Step powerUserRankingStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager
  ) {
    return new StepBuilder("powerUserRankingStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          powerUserBatch.execute(resolvePeriod(chunkContext.getStepContext().getJobParameters().get("period")));
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }

  private Period resolvePeriod(Object value) {
    if (value == null) {
      return Period.DAILY;
    }
    return Period.valueOf(value.toString());
  }
}
