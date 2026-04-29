package com.deokhugam.deokhugam_server.domain.user.repository;

import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PowerUserRepositoryCustom {
  List<PowerUser> findPowerUsersDynamic(Period period, Integer cursor, LocalDateTime after, String direction, int limit, LocalDate latestDate);
}
