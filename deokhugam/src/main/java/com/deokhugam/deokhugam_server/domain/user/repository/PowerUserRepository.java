package com.deokhugam.deokhugam_server.domain.user.repository;

import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID>, PowerUserRepositoryCustom {

  List<PowerUser> findAllByPeriodTypeAndCalculatedDate(Period periodType, LocalDate calculatedDate);

  long countByPeriodType(Period periodType);

  @Query("SELECT MAX(pu.calculatedDate) FROM PowerUser pu WHERE pu.periodType = :periodType")
  Optional<LocalDate> findMaxCalculatedDateByPeriodType(@Param("periodType") Period periodType);

}
