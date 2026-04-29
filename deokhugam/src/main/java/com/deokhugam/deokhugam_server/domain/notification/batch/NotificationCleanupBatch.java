package com.deokhugam.deokhugam_server.domain.notification.batch;

import com.deokhugam.deokhugam_server.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationCleanupBatch {

    private final NotificationService notificationService;

    public void execute() {
        notificationService.deleteExpiredNotifications();
    }
}
