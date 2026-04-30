package com.deokhugam.deokhugam_server.global.scheduler;

import com.deokhugam.deokhugam_server.global.log.DailyLogS3Uploader;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DailyLogUploadScheduler {

  private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

  private final DailyLogS3Uploader dailyLogS3Uploader;
  private final Clock clock;

  @Value("${deokhugam.log.s3.local-path:./logs}")
  private String localPath;

  @Autowired
  public DailyLogUploadScheduler(DailyLogS3Uploader dailyLogS3Uploader) {
    this(dailyLogS3Uploader, Clock.system(SEOUL_ZONE));
  }

  DailyLogUploadScheduler(DailyLogS3Uploader dailyLogS3Uploader, Clock clock) {
    this.dailyLogS3Uploader = dailyLogS3Uploader;
    this.clock = clock;
  }

  @Scheduled(cron = "${deokhugam.log.s3.upload-cron:0 0 1 * * *}", zone = "Asia/Seoul")
  public void uploadYesterdayLog() {
    LocalDate targetDate = LocalDate.now(clock).minusDays(1);
    log.info("Daily log S3 upload started. targetDate={}", targetDate);
    Path targetFile = Path.of(localPath, "deokhugam.%s.log".formatted(targetDate));

    dailyLogS3Uploader.upload(targetDate, targetFile);
  }
}
