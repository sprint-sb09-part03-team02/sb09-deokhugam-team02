package com.deokhugam.deokhugam_server.domain.notification.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID userId,
    UUID reviewId,
    String reviewContent,
    String message,
    boolean confirmed,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
