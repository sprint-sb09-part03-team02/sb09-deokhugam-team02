package com.deokhugam.deokhugam_server.domain.review.dto.request;

import jakarta.validation.constraints.*;

public record ReviewUpdateRequest(
    @NotBlank String content,
    @Min(1) @Max(5) int rating
) {}