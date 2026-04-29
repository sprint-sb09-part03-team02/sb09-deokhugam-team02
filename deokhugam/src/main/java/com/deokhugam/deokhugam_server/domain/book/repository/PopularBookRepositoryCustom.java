package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PopularBookRepositoryCustom {
  List<PopularBook> findPopularBooksDynamic(Period period, Integer cursor, LocalDateTime after, String direction, int limit, LocalDate latestDate);
}
