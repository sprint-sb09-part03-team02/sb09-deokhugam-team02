package com.deokhugam.deokhugam_server.domain.user.repository;

import com.deokhugam.deokhugam_server.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {
  Optional<User> findByEmail(String email);

  Optional<User> findByIdAndIsDeletedFalse(UUID id);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  List<User> findAllByIsDeletedTrueAndDeletedAtBefore(LocalDateTime dateTime);

}
