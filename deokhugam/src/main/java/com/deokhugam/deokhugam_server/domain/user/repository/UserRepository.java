package com.deokhugam.deokhugam_server.domain.user.repository;

import com.deokhugam.deokhugam_server.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  List<User> findAllByIsDeletedTrueAndDeletedAtBefore(LocalDateTime dateTime);

  @Query("SELECT u FROM User u WHERE u.createdAt >= :startTime AND u.isDeleted = false") // 일단 기본 조회로 시작!
  List<User> findPowerUsersWithPaging(
      @Param("startTime") LocalDateTime startTime,
      @Param("direction") String direction,
      @Param("cursor") String cursor,
      @Param("after") String after,
      @Param("limit") int limit
  );
}
