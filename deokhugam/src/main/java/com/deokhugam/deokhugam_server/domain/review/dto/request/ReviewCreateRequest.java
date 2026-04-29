package com.deokhugam.deokhugam_server.domain.review.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record ReviewCreateRequest(
    @NotNull(message = "도서 ID는 필수입니다.")
    UUID bookId,

    @NotNull(message = "사용자 ID는 필수입니다.") // 명세서 요구사항에 따라 추가 [cite: 1-224]
    UUID userId,

    @NotBlank(message = "리뷰 내용은 비어있을 수 없습니다.")
    String content,

    @Min(1) @Max(5)
    int rating
) {}