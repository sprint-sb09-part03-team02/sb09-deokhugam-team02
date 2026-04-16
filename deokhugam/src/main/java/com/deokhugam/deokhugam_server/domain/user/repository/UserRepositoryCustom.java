package com.deokhugam.deokhugam_server.domain.user.repository;

import com.deokhugam.deokhugam_server.domain.user.dto.response.UserRankQueryDto;
import java.time.LocalDate;
import java.util.List;

public interface UserRepositoryCustom {
  List<UserRankQueryDto> findUserActivityStatistics(LocalDate start, LocalDate end);

}
