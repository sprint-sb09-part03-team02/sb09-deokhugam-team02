package com.deokhugam.deokhugam_server.domain.notification.batch;

import com.deokhugam.deokhugam_server.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupBatch {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredNotifications() {
        log.info("[Batch] Notification cleanup started");
        notificationService.deleteExpiredNotifications();
        log.info("[Batch] Notification cleanup completed");
    }
}
