package com.deokhugam.deokhugam_server.domain.user.batch;

import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHardDeleteBatch {
  private final UserRepository userRepository;

  @Scheduled(cron = "${deokhugam.batch.user-hard-delete.cron:0 30 4 * * *}", zone = "Asia/Seoul")
  @Transactional
  public void cleanupOldDeletedUsers() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(1);

    List<User> targets = userRepository.findAllByIsDeletedTrueAndDeletedAtBefore(threshold);

    if (!targets.isEmpty()) {
      userRepository.deleteAllInBatch(targets);
    }
    log.info("[Batch] User hard delete completed. deletedCount={}", targets.size());
  }
}
