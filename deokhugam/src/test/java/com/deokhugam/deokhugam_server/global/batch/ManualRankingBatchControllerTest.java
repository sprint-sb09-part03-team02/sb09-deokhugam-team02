package com.deokhugam.deokhugam_server.global.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ManualRankingBatchControllerTest {

  private static final String MANUAL_BATCH_KEY = "test-manual-batch-key";

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job rankingJob;

  private ManualRankingBatchController controller;

  @BeforeEach
  void setUp() {
    controller = new ManualRankingBatchController(jobLauncher, rankingJob);
    ReflectionTestUtils.setField(controller, "manualBatchKey", MANUAL_BATCH_KEY);
  }

  @Test
  @DisplayName("성공: 전체 기간 랭킹 배치를 실행한다")
  void runRankingBatch_WithAllPeriod_LaunchesAllRankingJobs() throws Exception {
    when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
        .thenReturn(jobExecution(1L), jobExecution(2L), jobExecution(3L), jobExecution(4L));

    ResponseEntity<ManualRankingBatchController.ManualRankingBatchResponse> response =
        controller.runRankingBatch(MANUAL_BATCH_KEY, "ALL");

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().results())
        .extracting(ManualRankingBatchController.ManualRankingBatchResult::period)
        .containsExactly(Period.DAILY, Period.WEEKLY, Period.MONTHLY, Period.ALL_TIME);

    ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
    verify(jobLauncher, times(4)).run(any(Job.class), captor.capture());
    List<String> periods = captor.getAllValues().stream()
        .map(params -> params.getString("period"))
        .toList();
    assertThat(periods).containsExactly("DAILY", "WEEKLY", "MONTHLY", "ALL_TIME");
  }

  @Test
  @DisplayName("성공: 지정한 기간 랭킹 배치만 실행한다")
  void runRankingBatch_WithSinglePeriod_LaunchesRequestedRankingJob() throws Exception {
    when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution(10L));

    ResponseEntity<ManualRankingBatchController.ManualRankingBatchResponse> response =
        controller.runRankingBatch(MANUAL_BATCH_KEY, "weekly");

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().results()).hasSize(1);
    assertThat(response.getBody().results().get(0).period()).isEqualTo(Period.WEEKLY);
    assertThat(response.getBody().results().get(0).status()).isEqualTo(BatchStatus.COMPLETED.name());
    assertThat(response.getBody().results().get(0).executionId()).isEqualTo(10L);

    ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
    verify(jobLauncher).run(any(Job.class), captor.capture());
    assertThat(captor.getValue().getString("period")).isEqualTo("WEEKLY");
  }

  @Test
  @DisplayName("실패: 수동 배치 키가 일치하지 않으면 접근을 거부한다")
  void runRankingBatch_WithInvalidKey_ThrowsAccessDenied() {
    assertThatThrownBy(() -> controller.runRankingBatch("invalid-key", "DAILY"))
        .isInstanceOf(DeokhugamException.class)
        .hasMessage(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
  }

  @Test
  @DisplayName("실패: 수동 배치 키가 설정되지 않으면 접근을 거부한다")
  void runRankingBatch_WithBlankConfiguredKey_ThrowsAccessDenied() {
    ReflectionTestUtils.setField(controller, "manualBatchKey", "");

    assertThatThrownBy(() -> controller.runRankingBatch(MANUAL_BATCH_KEY, "DAILY"))
        .isInstanceOf(DeokhugamException.class)
        .hasMessage(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
  }

  @Test
  @DisplayName("실패: 지원하지 않는 기간이면 예외를 던진다")
  void runRankingBatch_WithInvalidPeriod_ThrowsInvalidPeriod() {
    assertThatThrownBy(() -> controller.runRankingBatch(MANUAL_BATCH_KEY, "YEARLY"))
        .isInstanceOf(DeokhugamException.class)
        .hasMessage(ErrorCode.INVALID_PERIOD.getMessage());
  }

  @Test
  @DisplayName("실패: 배치 실행 중 예외가 발생하면 서버 예외로 변환한다")
  void runRankingBatch_WhenJobLaunchFails_ThrowsInternalServerError() throws Exception {
    doThrow(new JobExecutionAlreadyRunningException("already running"))
        .when(jobLauncher)
        .run(any(Job.class), any(JobParameters.class));

    assertThatThrownBy(() -> controller.runRankingBatch(MANUAL_BATCH_KEY, "DAILY"))
        .isInstanceOf(DeokhugamException.class)
        .hasMessage(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
  }

  private JobExecution jobExecution(Long executionId) {
    JobExecution execution = new JobExecution(executionId);
    execution.setStatus(BatchStatus.COMPLETED);
    return execution;
  }
}
