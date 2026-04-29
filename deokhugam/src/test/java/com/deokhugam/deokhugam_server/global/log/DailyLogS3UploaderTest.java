package com.deokhugam.deokhugam_server.global.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class DailyLogS3UploaderTest {

  @Mock
  private S3Client s3Client;

  @TempDir
  private Path tempDir;

  @Test
  @DisplayName("성공: 날짜별 로그 파일을 S3 app/yyyy/MM/dd 경로로 업로드한다")
  void upload_Success() throws Exception {
    DailyLogS3Uploader uploader = uploader("deokhugam-logs-297904", "app", false);
    LocalDate logDate = LocalDate.of(2026, 4, 28);
    Path logFile = Files.writeString(tempDir.resolve("deokhugam.2026-04-28.log"), "test log");
    when(s3Client.putObject(any(PutObjectRequest.class), eq(logFile)))
        .thenReturn(PutObjectResponse.builder().build());

    boolean result = uploader.upload(logDate, logFile);

    ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(requestCaptor.capture(), eq(logFile));
    PutObjectRequest request = requestCaptor.getValue();
    assertThat(result).isTrue();
    assertThat(request.bucket()).isEqualTo("deokhugam-logs-297904");
    assertThat(request.key()).isEqualTo("app/2026/04/28/deokhugam.2026-04-28.log");
    assertThat(request.contentType()).isEqualTo("text/plain; charset=UTF-8");
  }

  @Test
  @DisplayName("성공: 로그 버킷이 없으면 업로드를 건너뛴다")
  void upload_Skip_WhenBucketBlank() throws Exception {
    DailyLogS3Uploader uploader = uploader("", "app", false);
    Path logFile = Files.writeString(tempDir.resolve("deokhugam.2026-04-28.log"), "test log");

    boolean result = uploader.upload(LocalDate.of(2026, 4, 28), logFile);

    assertThat(result).isFalse();
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName("성공: 로그 파일이 없으면 업로드를 건너뛴다")
  void upload_Skip_WhenFileDoesNotExist() {
    DailyLogS3Uploader uploader = uploader("deokhugam-logs-297904", "app", false);
    Path missingFile = tempDir.resolve("deokhugam.2026-04-28.log");

    boolean result = uploader.upload(LocalDate.of(2026, 4, 28), missingFile);

    assertThat(result).isFalse();
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName("성공: 업로드 후 삭제 설정이 켜져 있으면 로컬 로그 파일을 삭제한다")
  void upload_DeletesLocalFile_WhenDeleteAfterUploadIsEnabled() throws Exception {
    DailyLogS3Uploader uploader = uploader("deokhugam-logs-297904", "app", true);
    Path logFile = Files.writeString(tempDir.resolve("deokhugam.2026-04-28.log"), "test log");
    when(s3Client.putObject(any(PutObjectRequest.class), eq(logFile)))
        .thenReturn(PutObjectResponse.builder().build());

    boolean result = uploader.upload(LocalDate.of(2026, 4, 28), logFile);

    assertThat(result).isTrue();
    assertThat(logFile).doesNotExist();
  }

  @Test
  @DisplayName("실패: S3 업로드 실패 시 로그 업로드 예외를 던진다")
  void upload_ThrowsException_WhenS3UploadFails() throws Exception {
    DailyLogS3Uploader uploader = uploader("deokhugam-logs-297904", "app", false);
    Path logFile = Files.writeString(tempDir.resolve("deokhugam.2026-04-28.log"), "test log");
    when(s3Client.putObject(any(PutObjectRequest.class), eq(logFile)))
        .thenThrow(S3Exception.builder().message("upload failed").build());

    assertThatThrownBy(() -> uploader.upload(LocalDate.of(2026, 4, 28), logFile))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.S3_LOG_UPLOAD_FAILED);
  }

  private DailyLogS3Uploader uploader(String bucket, String prefix, boolean deleteAfterUpload) {
    DailyLogS3Uploader uploader = new DailyLogS3Uploader(s3Client);
    ReflectionTestUtils.setField(uploader, "bucket", bucket);
    ReflectionTestUtils.setField(uploader, "prefix", prefix);
    ReflectionTestUtils.setField(uploader, "deleteAfterUpload", deleteAfterUpload);
    return uploader;
  }
}
