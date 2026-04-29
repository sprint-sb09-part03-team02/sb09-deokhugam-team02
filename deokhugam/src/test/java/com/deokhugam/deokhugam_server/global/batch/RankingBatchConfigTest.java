package com.deokhugam.deokhugam_server.global.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.global.type.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
public class RankingBatchConfigTest {

  @Autowired(required = false)
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  @Qualifier("rankingJob")
  private Job rankingJob;

  @BeforeEach
  void setUp() {
    jobLauncherTestUtils.setJob(rankingJob);
  }

  @Test
  @DisplayName("성공: 랭킹 배치 Job이 성공적으로 완료된다")
  void rankingJob_Execution_Success() throws Exception {
    // given
    JobParameters jobParameters = new JobParametersBuilder()
      .addString("period", Period.DAILY.name())
      .addLong("requestedAt", System.currentTimeMillis())
      .toJobParameters();

    // when
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // then
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }
}
