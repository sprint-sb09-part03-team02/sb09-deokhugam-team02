package com.deokhugam.deokhugam_server.domain.notification.batch;

import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationCleanupBatchTest {

  @InjectMocks
  private NotificationCleanupBatch batch;

  @Mock
  private NotificationService notificationService;

  @Test
  @DisplayName("성공: 배치 실행 시 만료 알림 삭제 서비스를 호출한다")
  void execute() {
    batch.execute();

    verify(notificationService).deleteExpiredNotifications();
  }
}
