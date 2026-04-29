package com.deokhugam.deokhugam_server.global.log;

import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyLogS3Uploader {

  private final S3Client s3Client;

  @Value("${deokhugam.log.s3.bucket:}")
  private String bucket;

  @Value("${deokhugam.log.s3.prefix:app}")
  private String prefix;

  @Value("${deokhugam.log.s3.delete-after-upload:false}")
  private boolean deleteAfterUpload;

  public boolean upload(LocalDate logDate, Path logFile) {
    if (bucket == null || bucket.isBlank()) {
      log.warn("Daily log S3 upload skipped because S3_LOG_BUCKET is empty");
      return false;
    }
    if (!Files.isRegularFile(logFile)) {
      log.warn("Daily log S3 upload skipped because log file does not exist: {}", logFile);
      return false;
    }

    String key = buildKey(logDate, logFile.getFileName().toString());
    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .contentType("text/plain; charset=UTF-8")
          .build();

      s3Client.putObject(request, logFile);
      if (deleteAfterUpload) {
        Files.deleteIfExists(logFile);
      }
      log.info("Daily log S3 upload success: s3://{}/{}", bucket, key);
      return true;
    } catch (Exception e) {
      log.error("Daily log S3 upload failed: {}", e.getMessage(), e);
      throw new DeokhugamException(ErrorCode.S3_LOG_UPLOAD_FAILED);
    }
  }

  String buildKey(LocalDate logDate, String fileName) {
    String normalizedPrefix = prefix == null || prefix.isBlank()
        ? "app"
        : prefix.replaceAll("^/+", "").replaceAll("/+$", "");
    return "%s/%04d/%02d/%02d/%s".formatted(
        normalizedPrefix,
        logDate.getYear(),
        logDate.getMonthValue(),
        logDate.getDayOfMonth(),
        fileName
    );
  }
}
