package com.deokhugam.deokhugam_server.domain.notification.service;

import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationSearchRequest;
import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.deokhugam_server.domain.notification.dto.response.NotificationDto;
import com.deokhugam.deokhugam_server.domain.notification.entity.Notification;
import com.deokhugam.deokhugam_server.domain.notification.entity.NotificationType;
import com.deokhugam.deokhugam_server.domain.notification.mapper.NotificationMapper;
import com.deokhugam.deokhugam_server.domain.notification.repository.NotificationRepository;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.util.CursorPageUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationDto createNotification(UUID reviewId, UUID userId, NotificationType type, String content) {
        Notification notification = new Notification(reviewId, userId, type, content);
        return notificationMapper.toDto(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationDto readNotification(UUID notificationId, UUID requestUserId, NotificationUpdateRequest request) {
        Notification notification = notificationRepository.findByIdAndIsDeletedFalse(notificationId)
            .orElseThrow(() -> new DeokhugamException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(requestUserId)) {
            throw new DeokhugamException(ErrorCode.NOT_NOTIFICATION_OWNER);
        }

        notification.updateConfirmed(request.confirmed());
        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional
    public void readAllNotifications(UUID userId) {
        validateUserExists(userId);
        List<Notification> unread = notificationRepository.findUnreadByUserId(userId);
        unread.forEach(Notification::markAsRead);
    }

    @Override
    public CursorPageResponse<NotificationDto> getNotifications(NotificationSearchRequest request) {
        validateUserExists(request.getUserId());
        List<Notification> notifications = notificationRepository.searchNotifications(request);
        long totalElements = notificationRepository.countByUserId(request.getUserId());

        return CursorPageUtil.toResponse(
            notifications,
            request.getLimit(),
            totalElements,
            notificationMapper::toDto,
            n -> n.getId().toString(),
            Notification::getCreatedAt
        );
    }

    @Override
    @Transactional
    public void deleteExpiredNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<Notification> expired = notificationRepository.findExpiredReadNotifications(threshold);
        expired.forEach(Notification::delete);
    }

    private void validateUserExists(UUID userId) {
        userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND));
    }
}
