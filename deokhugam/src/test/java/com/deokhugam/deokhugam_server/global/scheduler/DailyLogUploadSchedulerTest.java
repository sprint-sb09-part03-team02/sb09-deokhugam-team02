package com.deokhugam.deokhugam_server.global.scheduler;

import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.global.log.DailyLogS3Uploader;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DailyLogUploadSchedulerTest {

  @Mock
  private DailyLogS3Uploader dailyLogS3Uploader;

  @TempDir
  private Path tempDir;

  @Test
  @DisplayName("성공: 매일 전날 날짜의 로그 파일 업로드를 요청한다")
  void uploadYesterdayLog_UploadsPreviousDayLog() {
    Clock fixedClock = Clock.fixed(
        Instant.parse("2026-04-28T01:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );
    DailyLogUploadScheduler scheduler = new DailyLogUploadScheduler(dailyLogS3Uploader, fixedClock);
    ReflectionTestUtils.setField(scheduler, "localPath", tempDir.toString());

    scheduler.uploadYesterdayLog();

    LocalDate expectedDate = LocalDate.of(2026, 4, 27);
    Path expectedFile = tempDir.resolve("deokhugam.2026-04-27.log");
    verify(dailyLogS3Uploader).upload(expectedDate, expectedFile);
  }
}
