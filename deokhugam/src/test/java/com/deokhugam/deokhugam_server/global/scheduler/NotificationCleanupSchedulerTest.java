package com.deokhugam.deokhugam_server.global.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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
class NotificationCleanupSchedulerTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job notificationCleanupJob;

  @Test
  @DisplayName("성공: 알림 정리 배치를 실행한다")
  void runNotificationCleanup_LaunchesJob() throws Exception {
    NotificationCleanupScheduler scheduler = new NotificationCleanupScheduler(
        jobLauncher,
        notificationCleanupJob
    );

    scheduler.runNotificationCleanup();

    ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
    verify(jobLauncher).run(any(Job.class), captor.capture());
    assertThat(captor.getValue().getLong("requestedAt")).isNotNull();
  }

  @Test
  @DisplayName("실패: 알림 정리 배치 실행 예외는 스케줄러 밖으로 전파하지 않는다")
  void runNotificationCleanup_WhenJobLaunchFails_DoesNotThrow() throws Exception {
    NotificationCleanupScheduler scheduler = new NotificationCleanupScheduler(
        jobLauncher,
        notificationCleanupJob
    );
    doThrow(new JobExecutionAlreadyRunningException("already running"))
        .when(jobLauncher)
        .run(any(Job.class), any(JobParameters.class));

    scheduler.runNotificationCleanup();

    verify(jobLauncher).run(any(Job.class), any(JobParameters.class));
  }
}
