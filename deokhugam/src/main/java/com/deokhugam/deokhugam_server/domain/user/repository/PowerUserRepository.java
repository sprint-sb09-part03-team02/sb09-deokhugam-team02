package com.deokhugam.deokhugam_server.domain.user.repository;

import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {
  @Query("SELECT p FROM PowerUser p " +
      "JOIN FETCH p.user u " +
      "WHERE p.periodType = :period " +
      "AND (" +
      "  :after IS NULL OR " +
      "  (:direction = 'DESC' AND (p.createdAt < :after OR (p.createdAt = :after AND p.rankOrder > :cursor))) OR " +
      "  (:direction = 'ASC' AND (p.createdAt > :after OR (p.createdAt = :after AND p.rankOrder < :cursor)))" +
      ") " +
      "ORDER BY " +
      "  CASE WHEN :direction = 'DESC' THEN p.createdAt END DESC, " +
      "  CASE WHEN :direction = 'DESC' THEN p.rankOrder END ASC, " +
      "  CASE WHEN :direction = 'ASC' THEN p.createdAt END ASC, " +
      "  CASE WHEN :direction = 'ASC' THEN p.rankOrder END DESC")
  List<PowerUser> findPowerUsersByRequirements(
      @Param("period") Period period,
      @Param("direction") String direction,
      @Param("cursor") Integer cursor,
      @Param("after") LocalDateTime after,
      @Param("limit") int limit
  );

  List<PowerUser> findAllByPeriodTypeAndCalculatedDate(Period periodType, LocalDate calculatedDate);
}
