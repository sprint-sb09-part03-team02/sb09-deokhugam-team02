package com.deokhugam.deokhugam_server.domain.notification.dto.response;

import com.deokhugam.deokhugam_server.domain.notification.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID reviewId,
    UUID userId,
    NotificationType type,
    String content,
    boolean isRead,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
