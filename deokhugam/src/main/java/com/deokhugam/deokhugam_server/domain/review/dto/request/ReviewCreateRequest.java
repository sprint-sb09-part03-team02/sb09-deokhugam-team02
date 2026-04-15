package com.deokhugam.deokhugam_server.domain.review.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record ReviewCreateRequest(
    @NotNull UUID bookId,
    @NotBlank String content,
    @Min(1) @Max(5) int rating
) {}