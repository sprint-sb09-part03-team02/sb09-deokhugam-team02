package com.deokhugam.deokhugam_server.global.batch;

import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/batches/ranking")
@RequiredArgsConstructor
public class ManualRankingBatchController {

  private static final String INTERNAL_BATCH_KEY_HEADER = "X-Internal-Batch-Key";
  private static final String ALL_PERIODS = "ALL";

  private final JobLauncher jobLauncher;

  @Qualifier("rankingJob")
  private final Job rankingJob;

  @Value("${deokhugam.batch.manual.key:}")
  private String manualBatchKey;

  @PostMapping
  public ResponseEntity<ManualRankingBatchResponse> runRankingBatch(
      @RequestHeader(INTERNAL_BATCH_KEY_HEADER) String requestKey,
      @RequestParam(defaultValue = ALL_PERIODS) String period
  ) {
    validateInternalBatchKey(requestKey);

    List<Period> periods = resolvePeriods(period);
    List<ManualRankingBatchResult> results = new ArrayList<>();
    long requestedAt = System.currentTimeMillis();

    for (Period targetPeriod : periods) {
      results.add(runJob(targetPeriod, requestedAt));
    }

    return ResponseEntity.ok(new ManualRankingBatchResponse(results));
  }

  private void validateInternalBatchKey(String requestKey) {
    if (!StringUtils.hasText(manualBatchKey) || !manualBatchKey.equals(requestKey)) {
      throw new DeokhugamException(ErrorCode.HANDLE_ACCESS_DENIED);
    }
  }

  private List<Period> resolvePeriods(String period) {
    if (ALL_PERIODS.equalsIgnoreCase(period)) {
      return Arrays.asList(Period.values());
    }
    try {
      return List.of(Period.valueOf(period.toUpperCase()));
    } catch (IllegalArgumentException e) {
      throw new DeokhugamException(ErrorCode.INVALID_PERIOD);
    }
  }

  private ManualRankingBatchResult runJob(Period period, long requestedAt) {
    try {
      JobExecution execution = jobLauncher.run(rankingJob, new JobParametersBuilder()
          .addString("period", period.name())
          .addLong("requestedAt", requestedAt)
          .toJobParameters());

      BatchStatus status = execution.getStatus();
      log.info("[ManualBatch] Ranking job completed. period={}, status={}, executionId={}",
          period, status, execution.getId());
      return new ManualRankingBatchResult(period, status.name(), execution.getId());
    } catch (Exception e) {
      log.error("[ManualBatch] Ranking job failed. period={}", period, e);
      throw new DeokhugamException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  public record ManualRankingBatchResponse(List<ManualRankingBatchResult> results) {
  }

  public record ManualRankingBatchResult(Period period, String status, Long executionId) {
  }
}
