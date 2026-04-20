package com.deokhugam.deokhugam_server.domain.notification.repository;

import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationSearchRequest;
import com.deokhugam.deokhugam_server.domain.notification.entity.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryCustom {

    List<Notification> searchNotifications(NotificationSearchRequest request);

    long countByUserId(UUID userId);
}
