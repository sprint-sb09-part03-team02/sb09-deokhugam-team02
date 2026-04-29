package com.deokhugam.deokhugam_server.domain.user.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserHardDeleteBatchTest {

  @InjectMocks
  private UserHardDeleteBatch userHardDeleteBatch;

  @Mock
  private UserRepository userRepository;

  @Test
  @DisplayName("성공: 삭제 대상 유저가 존재하면 deleteAllInBatch를 호출한다")
  void cleanupOldDeletedUsers_WhenTargetsExist() {
    // given
    User mockUser = User.builder().build();
    List<User> targets = List.of(mockUser);

    // threshold가 LocalDateTime.now() 기준이라 any()로 매칭해야 에러가 안 납니다.
    when(userRepository.findAllByIsDeletedTrueAndDeletedAtBefore(any(LocalDateTime.class)))
      .thenReturn(targets);

    // when
    userHardDeleteBatch.cleanupOldDeletedUsers();

    // then
    verify(userRepository).deleteAllInBatch(targets);
  }

  @Test
  @DisplayName("성공: 삭제 대상 유저가 없으면 deleteAllInBatch를 호출하지 않는다")
  void cleanupOldDeletedUsers_WhenNoTargets() {
    // given
    when(userRepository.findAllByIsDeletedTrueAndDeletedAtBefore(any(LocalDateTime.class)))
      .thenReturn(Collections.emptyList());

    // when
    userHardDeleteBatch.cleanupOldDeletedUsers();

    // then
    verify(userRepository, never()).deleteAllInBatch(any());
  }
}
