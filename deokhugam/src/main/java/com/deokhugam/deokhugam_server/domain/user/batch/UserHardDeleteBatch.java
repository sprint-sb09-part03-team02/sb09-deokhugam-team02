package com.deokhugam.deokhugam_server.domain.user.batch;

import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserHardDeleteBatch {
  private final UserRepository userRepository;

  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void cleanupOldDeletedUsers() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(1);

    List<User> targets = userRepository.findAllByIsDeletedTrueAndDeletedAtBefore(threshold);

    if (!targets.isEmpty()) {
      userRepository.deleteAll(targets);
    }
  }


}
