package com.deokhugam.deokhugam_server.global.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;

@ExtendWith(MockitoExtension.class)
class RankingSchedulerTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job rankingJob;

  @Test
  @DisplayName("성공: 모든 기간 랭킹 배치를 함께 실행한다")
  void runDailyAndAllTimeRanking_LaunchesDailyAndAllTimeJobs() throws Exception {
    RankingScheduler scheduler = new RankingScheduler(jobLauncher, rankingJob);

    scheduler.runDailyAndAllTimeRanking();

    ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
    verify(jobLauncher, times(4)).run(any(Job.class), captor.capture());
    List<String> periods = captor.getAllValues().stream()
        .map(params -> params.getString("period"))
        .toList();
    assertThat(periods).containsExactly(
        Period.DAILY.name(),
        Period.WEEKLY.name(),
        Period.MONTHLY.name(),
        Period.ALL_TIME.name()
    );
  }

  @Test
  @DisplayName("성공: 주간 랭킹 배치를 실행한다")
  void runWeeklyRanking_LaunchesWeeklyJob() throws Exception {
    RankingScheduler scheduler = new RankingScheduler(jobLauncher, rankingJob);

    scheduler.runWeeklyRanking();

    ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
    verify(jobLauncher).run(any(Job.class), captor.capture());
    assertThat(captor.getValue().getString("period")).isEqualTo(Period.WEEKLY.name());
  }

  @Test
  @DisplayName("성공: 월간 랭킹 배치를 실행한다")
  void runMonthlyRanking_LaunchesMonthlyJob() throws Exception {
    RankingScheduler scheduler = new RankingScheduler(jobLauncher, rankingJob);

    scheduler.runMonthlyRanking();

    ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
    verify(jobLauncher).run(any(Job.class), captor.capture());
    assertThat(captor.getValue().getString("period")).isEqualTo(Period.MONTHLY.name());
  }

  @Test
  @DisplayName("실패: 랭킹 배치 실행 예외는 스케줄러 밖으로 전파하지 않는다")
  void runWeeklyRanking_WhenJobLaunchFails_DoesNotThrow() throws Exception {
    RankingScheduler scheduler = new RankingScheduler(jobLauncher, rankingJob);
    doThrow(new JobExecutionAlreadyRunningException("already running"))
        .when(jobLauncher)
        .run(any(Job.class), any(JobParameters.class));

    scheduler.runWeeklyRanking();

    verify(jobLauncher).run(any(Job.class), any(JobParameters.class));
  }
}
