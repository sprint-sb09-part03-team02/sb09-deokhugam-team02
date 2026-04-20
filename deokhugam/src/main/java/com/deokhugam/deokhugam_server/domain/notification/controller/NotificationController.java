package com.deokhugam.deokhugam_server.domain.notification.controller;

import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationSearchRequest;
import com.deokhugam.deokhugam_server.domain.notification.dto.response.NotificationDto;
import com.deokhugam.deokhugam_server.domain.notification.service.NotificationService;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<CursorPageResponse<NotificationDto>> getNotifications(
        @Valid NotificationSearchRequest request,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {

        request.setUserId(requestUserId);
        return ResponseEntity.ok(notificationService.getNotifications(request));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDto> readNotification(
        @PathVariable UUID notificationId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {

        return ResponseEntity.ok(notificationService.readNotification(notificationId, requestUserId));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> readAllNotifications(
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {

        notificationService.readAllNotifications(requestUserId);
        return ResponseEntity.noContent().build();
    }
}
