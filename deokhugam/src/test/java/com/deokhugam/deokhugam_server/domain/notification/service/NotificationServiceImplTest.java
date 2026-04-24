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
import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationSearchRequest;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import java.time.LocalDateTime;
import java.util.List;
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

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("성공: confirmed 값에 따라 알림 읽음 상태를 업데이트한다")
    void readNotification_Success() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(UUID.randomUUID(), userId, NotificationType.REVIEW_COMMENTED, "알림");
        NotificationDto notificationDto = new NotificationDto(
            notificationId,
            userId,
            notification.getReviewId(),
            "리뷰 내용",
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

    @Test
    @DisplayName("성공: 알림 목록 조회 시 사용자 존재를 확인하고 결과를 반환한다")
    void getNotifications_Success() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification(UUID.randomUUID(), userId, NotificationType.REVIEW_COMMENTED, "알림");
        ReflectionTestUtils.setField(notification, "id", notificationId);

        NotificationSearchRequest request = new NotificationSearchRequest();
        ReflectionTestUtils.setField(request, "userId", userId);
        ReflectionTestUtils.setField(request, "limit", 20);

        NotificationDto notificationDto = new NotificationDto(
            notificationId,
            userId,
            notification.getReviewId(),
            "리뷰 내용",
            notification.getContent(),
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(org.mockito.Mockito.mock(User.class)));
        given(notificationRepository.searchNotifications(request)).willReturn(List.of(notification));
        given(notificationRepository.countByUserId(userId)).willReturn(1L);
        given(notificationMapper.toDto(notification)).willReturn(notificationDto);

        CursorPageResponse<NotificationDto> response = notificationService.getNotifications(request);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).message()).isEqualTo("알림");
    }

    @Test
    @DisplayName("실패: 알림 목록 조회 시 사용자가 없으면 예외가 발생한다")
    void getNotifications_Fail_WhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        NotificationSearchRequest request = new NotificationSearchRequest();
        ReflectionTestUtils.setField(request, "userId", userId);

        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotifications(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패: 모든 알림 읽음 처리 시 사용자가 없으면 예외가 발생한다")
    void readAllNotifications_Fail_WhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.readAllNotifications(userId))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}
