package com.deokhugam.deokhugam_server.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.deokhugam_server.domain.notification.dto.response.NotificationDto;
import com.deokhugam.deokhugam_server.domain.notification.entity.Notification;
import com.deokhugam.deokhugam_server.domain.notification.entity.NotificationType;
import com.deokhugam.deokhugam_server.domain.notification.mapper.NotificationMapper;
import com.deokhugam.deokhugam_server.domain.notification.repository.NotificationRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Test
    @DisplayName("성공: confirmed 값에 따라 알림 읽음 상태를 업데이트한다")
    void readNotification_Success() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(UUID.randomUUID(), userId, NotificationType.REVIEW_COMMENTED, "알림");
        NotificationDto notificationDto = new NotificationDto(
            notificationId,
            notification.getReviewId(),
            userId,
            notification.getType(),
            notification.getContent(),
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        ReflectionTestUtils.setField(notification, "id", notificationId);
        notification.markAsRead();

        given(notificationRepository.findByIdAndIsDeletedFalse(notificationId)).willReturn(Optional.of(notification));
        given(notificationMapper.toDto(notification)).willReturn(notificationDto);

        notificationService.readNotification(notificationId, userId, new NotificationUpdateRequest(false));

        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("실패: 알림 소유자가 아니면 예외가 발생한다")
    void readNotification_Fail_WhenNotOwner() {
        UUID notificationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        Notification notification = new Notification(UUID.randomUUID(), ownerId, NotificationType.REVIEW_COMMENTED, "알림");
        ReflectionTestUtils.setField(notification, "id", notificationId);

        given(notificationRepository.findByIdAndIsDeletedFalse(notificationId)).willReturn(Optional.of(notification));

        assertThatThrownBy(() ->
            notificationService.readNotification(notificationId, requestUserId, new NotificationUpdateRequest(true)))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.NOT_NOTIFICATION_OWNER.getMessage());
    }
}
