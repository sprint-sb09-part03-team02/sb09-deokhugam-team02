package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {
  List<PopularBook> findAllByPeriodTypeAndCalculatedDate(Period periodType, LocalDate calculatedDate);
}
