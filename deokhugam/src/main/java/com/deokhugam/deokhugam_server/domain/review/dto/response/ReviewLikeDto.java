package com.deokhugam.deokhugam_server.domain.review.dto.response;

import java.util.UUID;

public record ReviewLikeDto(
    UUID reviewId,
    UUID userId,
    boolean liked
) {}