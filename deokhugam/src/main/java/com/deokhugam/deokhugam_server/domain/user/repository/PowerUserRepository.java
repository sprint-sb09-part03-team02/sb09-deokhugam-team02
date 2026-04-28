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
    "AND (:after IS NULL OR p.createdAt < :after OR (p.createdAt = :after AND p.rankOrder > :cursor)) " +
    "ORDER BY p.createdAt DESC, p.rankOrder DESC")
  List<PowerUser> findPowerUsersDesc(
    @Param("period") Period period,
    @Param("cursor") Integer cursor,
    @Param("after") LocalDateTime after,
    Limit limit
  );

  @Query("SELECT p FROM PowerUser p " +
    "JOIN FETCH p.user u " +
    "WHERE p.periodType = :period " +
    "AND (:after IS NULL OR p.createdAt > :after OR (p.createdAt = :after AND p.rankOrder < :cursor)) " +
    "ORDER BY p.createdAt ASC, p.rankOrder ASC")
  List<PowerUser> findPowerUsersAsc(
    @Param("period") Period period,
    @Param("cursor") Integer cursor,
    @Param("after") LocalDateTime after,
    Limit limit
  );
  List<PowerUser> findAllByPeriodTypeAndCalculatedDate(Period periodType, LocalDate calculatedDate);

  long countByPeriodType(Period periodType);
}
