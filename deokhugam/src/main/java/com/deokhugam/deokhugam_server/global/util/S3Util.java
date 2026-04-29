package com.deokhugam.deokhugam_server.global.util;

import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Util {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${cloud.aws.s3.presigned-url-expiration:3600}")
  private long presignedUrlExpirationSeconds;

  /**
   * MultipartFile을 S3에 업로드하고 퍼블릭 URL을 반환합니다.
   *
   * @param file   업로드할 파일
   * @param folder S3 내 저장 폴더 (예: "books", "profiles")
   * @return 업로드된 파일의 S3 URL
   */
  public String upload(MultipartFile file, String folder) {
    String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
    try (InputStream is = file.getInputStream()) {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();

      s3Client.putObject(request, RequestBody.fromInputStream(is, file.getSize()));
      log.info("S3 upload success: {}", key);
      return buildUrl(key);
    } catch (Exception e) {
      log.error("S3 upload failed: {}", e.getMessage(), e);
      throw new DeokhugamException(ErrorCode.S3_UPLOAD_FAILED);
    }
  }

  public void delete(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return;
    }
    try {
      String key = extractKeyFromUrl(fileUrl);
      DeleteObjectRequest request = DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build();
      s3Client.deleteObject(request);
      log.info("S3 delete success: {}", key);
    } catch (Exception e) {
      log.error("S3 delete failed: {}", e.getMessage(), e);
      throw new DeokhugamException(ErrorCode.S3_DELETE_FAILED);
    }
  }

  public String toPresignedUrlIfS3Url(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank() || !isManagedS3Url(fileUrl)) {
      return fileUrl;
    }

    String key = extractKeyFromUrl(fileUrl);
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
        .getObjectRequest(getObjectRequest)
        .build();

    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }

  private String buildUrl(String key) {
    return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
  }

  private boolean isManagedS3Url(String url) {
    return url.startsWith("https://" + bucket + ".s3." + region + ".amazonaws.com/");
  }

  private String extractKeyFromUrl(String url) {
    // https://bucket.s3.region.amazonaws.com/key 형식에서 key 추출
    String prefix = "amazonaws.com/";
    int idx = url.indexOf(prefix);
    if (idx == -1) {
      throw new IllegalArgumentException("올바르지 않은 S3 URL 형식입니다: " + url);
    }
    return url.substring(idx + prefix.length());
  }
}
