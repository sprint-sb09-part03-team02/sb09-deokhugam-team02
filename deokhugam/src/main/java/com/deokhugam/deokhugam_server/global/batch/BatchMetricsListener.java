package com.deokhugam.deokhugam_server.global.batch;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.BatchStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchMetricsListener implements JobExecutionListener {

  private final MeterRegistry meterRegistry;
  private final Map<String, AtomicLong> lastDurationSeconds = new ConcurrentHashMap<>();
  private final Map<String, AtomicLong> lastStatus = new ConcurrentHashMap<>();

  @Override
  public void afterJob(JobExecution jobExecution) {
    String jobName = jobExecution.getJobInstance().getJobName();
    String status = jobExecution.getStatus().name();
    long durationSeconds = calculateDurationSeconds(jobExecution);

    meterRegistry.counter("deokhugam.batch.job.completed", "job", jobName, "status", status)
        .increment();
    Timer.builder("deokhugam.batch.job.duration")
        .tag("job", jobName)
        .tag("status", status)
        .register(meterRegistry)
        .record(Duration.ofSeconds(durationSeconds));

    durationGauge(jobName).set(durationSeconds);
    statusGauge(jobName).set(jobExecution.getStatus() == BatchStatus.COMPLETED ? 1 : 0);
  }

  private long calculateDurationSeconds(JobExecution jobExecution) {
    LocalDateTime startTime = jobExecution.getStartTime();
    LocalDateTime endTime = jobExecution.getEndTime();
    if (startTime == null || endTime == null) {
      return 0;
    }
    return Math.max(0, Duration.between(startTime, endTime).toSeconds());
  }

  private AtomicLong durationGauge(String jobName) {
    return lastDurationSeconds.computeIfAbsent(jobName, key -> {
      AtomicLong value = new AtomicLong();
      Gauge.builder("deokhugam.batch.job.last.duration.seconds", value, AtomicLong::get)
          .tag("job", key)
          .register(meterRegistry);
      return value;
    });
  }

  private AtomicLong statusGauge(String jobName) {
    return lastStatus.computeIfAbsent(jobName, key -> {
      AtomicLong value = new AtomicLong();
      Gauge.builder("deokhugam.batch.job.last.success", value, AtomicLong::get)
          .tag("job", key)
          .register(meterRegistry);
      return value;
    });
  }
}
