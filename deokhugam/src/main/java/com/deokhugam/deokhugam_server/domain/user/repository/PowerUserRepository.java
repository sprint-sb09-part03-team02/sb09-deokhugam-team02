package com.deokhugam.deokhugam_server.domain.user.repository;

import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Limit;
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
    "  CAST(:after AS LocalDateTime) IS NULL OR " +
    "  (CAST(:direction AS string) = 'DESC' AND (p.createdAt < :after OR (p.createdAt = :after AND p.rankOrder > :cursor))) OR " +
    "  (CAST(:direction AS string) = 'ASC' AND (p.createdAt > :after OR (p.createdAt = :after AND p.rankOrder < :cursor)))" +
    ") " +
    "ORDER BY " +
    "  CASE WHEN CAST(:direction AS string) = 'DESC' THEN p.createdAt END DESC, " +
    "  CASE WHEN CAST(:direction AS string) = 'DESC' THEN p.rankOrder END ASC, " +
    "  CASE WHEN CAST(:direction AS string) = 'ASC' THEN p.createdAt END ASC, " +
    "  CASE WHEN CAST(:direction AS string) = 'ASC' THEN p.rankOrder END DESC")
  List<PowerUser> findPowerUsersByRequirements(
      @Param("period") Period period,
      @Param("direction") String direction,
      @Param("cursor") Integer cursor,
      @Param("after") LocalDateTime after,
      @Param("limit") Limit limit
  );

  List<PowerUser> findAllByPeriodTypeAndCalculatedDate(Period periodType, LocalDate calculatedDate);

  long countByPeriodType(Period periodType);
}
