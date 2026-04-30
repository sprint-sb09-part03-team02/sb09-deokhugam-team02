package com.deokhugam.deokhugam_server.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3UtilTest {

  private static final String BUCKET = "deokhugam-storage";
  private static final String REGION = "ap-northeast-2";

  @Mock
  private S3Client s3Client;

  @Mock
  private S3Presigner s3Presigner;

  private S3Util s3Util;

  @BeforeEach
  void setUp() {
    s3Util = new S3Util(s3Client, s3Presigner);
    ReflectionTestUtils.setField(s3Util, "bucket", BUCKET);
    ReflectionTestUtils.setField(s3Util, "region", REGION);
    ReflectionTestUtils.setField(s3Util, "presignedUrlExpirationSeconds", 3600L);
  }

  @Test
  @DisplayName("성공: 파일을 S3에 업로드하고 관리 URL을 반환한다")
  void upload_Success() {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "cover.png",
        "image/png",
        "image".getBytes()
    );
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());

    String result = s3Util.upload(file, "books");

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
    PutObjectRequest request = captor.getValue();
    assertThat(request.bucket()).isEqualTo(BUCKET);
    assertThat(request.key()).startsWith("books/").endsWith("_cover.png");
    assertThat(request.contentType()).isEqualTo("image/png");
    assertThat(request.contentLength()).isEqualTo(file.getSize());
    assertThat(result).startsWith("https://deokhugam-storage.s3.ap-northeast-2.amazonaws.com/books/");
    assertThat(result).endsWith("_cover.png");
  }

  @Test
  @DisplayName("실패: 파일 업로드 중 예외가 발생하면 S3 업로드 예외를 던진다")
  void upload_WhenFails_ThrowsException() throws Exception {
    MockMultipartFile file = new FailingMultipartFile();

    assertThatThrownBy(() -> s3Util.upload(file, "books"))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.S3_UPLOAD_FAILED);
  }

  @Test
  @DisplayName("성공: null 또는 blank URL 삭제는 건너뛴다")
  void delete_NullOrBlank_SkipsDelete() {
    s3Util.delete(null);
    s3Util.delete(" ");

    verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  @DisplayName("성공: S3 URL에서 key를 추출해 객체를 삭제한다")
  void delete_Success() {
    String fileUrl = managedUrl("books/cover.png");
    when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(DeleteObjectResponse.builder().build());

    s3Util.delete(fileUrl);

    ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3Client).deleteObject(captor.capture());
    assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
    assertThat(captor.getValue().key()).isEqualTo("books/cover.png");
  }

  @Test
  @DisplayName("실패: S3 형식이 아닌 URL 삭제 시 삭제 예외를 던진다")
  void delete_InvalidUrl_ThrowsException() {
    assertThatThrownBy(() -> s3Util.delete("https://example.com/image.png"))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.S3_DELETE_FAILED);
  }

  @Test
  @DisplayName("성공: 관리 대상 S3 URL이면 presigned URL로 변환한다")
  void toPresignedUrlIfS3Url_ManagedUrl_ReturnsPresignedUrl() throws Exception {
    String fileUrl = managedUrl("books/cover.png");
    PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
    when(presignedRequest.url())
        .thenReturn(new URL("https://presigned.example.com/books/cover.png"));
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .thenReturn(presignedRequest);

    String result = s3Util.toPresignedUrlIfS3Url(fileUrl);

    assertThat(result).isEqualTo("https://presigned.example.com/books/cover.png");
    verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
  }

  @Test
  @DisplayName("성공: 관리 대상이 아닌 URL은 그대로 반환한다")
  void toPresignedUrlIfS3Url_NotManaged_ReturnsOriginal() {
    assertThat(s3Util.toPresignedUrlIfS3Url(null)).isNull();
    assertThat(s3Util.toPresignedUrlIfS3Url(" ")).isEqualTo(" ");
    assertThat(s3Util.toPresignedUrlIfS3Url("https://example.com/image.png"))
        .isEqualTo("https://example.com/image.png");

    verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
  }

  private String managedUrl(String key) {
    return "https://%s.s3.%s.amazonaws.com/%s".formatted(BUCKET, REGION, key);
  }

  private static class FailingMultipartFile extends MockMultipartFile {

    FailingMultipartFile() {
      super("file", "cover.png", "image/png", new byte[0]);
    }

    @Override
    public java.io.InputStream getInputStream() throws IOException {
      throw new IOException("read failed");
    }
  }
}
