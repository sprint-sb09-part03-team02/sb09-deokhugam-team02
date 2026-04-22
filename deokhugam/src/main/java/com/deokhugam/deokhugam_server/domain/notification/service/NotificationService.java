package com.deokhugam.deokhugam_server.domain.notification.service;

import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationSearchRequest;
import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.deokhugam_server.domain.notification.dto.response.NotificationDto;
import com.deokhugam.deokhugam_server.domain.notification.entity.NotificationType;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import java.util.UUID;

public interface NotificationService {

    NotificationDto createNotification(UUID reviewId, UUID userId, NotificationType type, String content);

    NotificationDto readNotification(UUID notificationId, UUID requestUserId, NotificationUpdateRequest request);

    void readAllNotifications(UUID userId);

    CursorPageResponse<NotificationDto> getNotifications(NotificationSearchRequest request);

    void deleteExpiredNotifications();
}
