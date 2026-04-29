package com.deokhugam.deokhugam_server.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationSearchRequest;
import com.deokhugam.deokhugam_server.domain.notification.entity.Notification;
import com.deokhugam.deokhugam_server.domain.notification.entity.NotificationType;
import com.deokhugam.deokhugam_server.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class NotificationRepositoryImplTest {

  private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 4, 28, 10, 0);
  private static final LocalDateTime FIXED_OLD = FIXED_NOW.minusDays(1);

  @Autowired private NotificationRepository notificationRepository;
  @Autowired private EntityManager em;

  private UUID userId;
  private UUID otherUserId;
  private UUID newestNotificationId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    otherUserId = UUID.randomUUID();

    Notification newestNotification = createNotification(userId, "최신 알림", FIXED_NOW);
    createNotification(userId, "이전 알림", FIXED_OLD);
    createNotification(otherUserId, "다른 사용자 알림", FIXED_NOW);

    Notification readNotification = createNotification(userId, "읽은 알림", FIXED_NOW.minusHours(1));
    readNotification.markAsRead();

    Notification deletedNotification = createNotification(userId, "삭제된 알림", FIXED_NOW.minusHours(2));
    deletedNotification.delete();

    em.flush();
    newestNotificationId = newestNotification.getId();

    updateCreatedAt("최신 알림", FIXED_NOW);
    updateCreatedAt("읽은 알림", FIXED_NOW.minusHours(1));
    updateCreatedAt("삭제된 알림", FIXED_NOW.minusHours(2));
    updateCreatedAt("이전 알림", FIXED_OLD);
    updateCreatedAt("다른 사용자 알림", FIXED_NOW);

    em.clear();
  }

  @Test
  @DisplayName("성공: 사용자별 알림을 최신순으로 조회한다")
  void searchNotifications_Desc_Success() {
    // given
    NotificationSearchRequest request = new NotificationSearchRequest();
    request.setUserId(userId);
    request.setDirection("DESC");
    request.setLimit(10);

    // when
    List<Notification> result = notificationRepository.searchNotifications(request);

    // then
    assertThat(result).extracting(Notification::getContent)
        .containsExactly("최신 알림", "읽은 알림", "이전 알림");
  }

  @Test
  @DisplayName("성공: 커서 이후의 알림만 조회한다")
  void searchNotifications_Cursor_Success() {
    // given
    NotificationSearchRequest request = new NotificationSearchRequest();
    request.setUserId(userId);
    request.setAfter(FIXED_NOW);
    request.setCursor(newestNotificationId.toString());
    request.setDirection("DESC");
    request.setLimit(10);

    // when
    List<Notification> result = notificationRepository.searchNotifications(request);

    // then
    assertThat(result).extracting(Notification::getContent)
        .containsExactly("읽은 알림", "이전 알림");
  }

  @Test
  @DisplayName("성공: 오름차순으로 알림을 조회한다")
  void searchNotifications_Asc_Success() {
    // given
    NotificationSearchRequest request = new NotificationSearchRequest();
    request.setUserId(userId);
    request.setDirection("ASC");
    request.setLimit(10);

    // when
    List<Notification> result = notificationRepository.searchNotifications(request);

    // then
    assertThat(result).extracting(Notification::getContent)
        .containsExactly("이전 알림", "읽은 알림", "최신 알림");
  }

  @Test
  @DisplayName("성공: 읽지 않은 알림만 조회한다")
  void findUnreadByUserId_Success() {
    // when
    List<Notification> result = notificationRepository.findUnreadByUserId(userId);

    // then
    assertThat(result).extracting(Notification::getContent)
        .containsExactlyInAnyOrder("이전 알림", "최신 알림");
  }

  @Test
  @DisplayName("성공: 삭제되지 않은 사용자 알림 수만 계산한다")
  void countByUserId_Success() {
    // when
    long count = notificationRepository.countByUserId(userId);

    // then
    assertThat(count).isEqualTo(3);
  }

  private Notification createNotification(UUID targetUserId, String content, LocalDateTime createdAt) {
    Notification notification = new Notification(
        UUID.randomUUID(),
        targetUserId,
        NotificationType.REVIEW_COMMENTED,
        content
    );
    ReflectionTestUtils.setField(notification, "createdAt", createdAt);
    ReflectionTestUtils.setField(notification, "updatedAt", createdAt);
    em.persist(notification);
    return notification;
  }

  private void updateCreatedAt(String content, LocalDateTime createdAt) {
    em.createNativeQuery("UPDATE notifications SET created_at = ?1 WHERE content = ?2")
        .setParameter(1, createdAt)
        .setParameter(2, content)
        .executeUpdate();
  }
}
