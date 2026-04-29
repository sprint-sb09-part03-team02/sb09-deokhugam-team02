package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID>, PopularBookRepositoryCustom {

  List<PopularBook> findAllByPeriodTypeAndCalculatedDate(Period periodType, LocalDate calculatedDate);

  long countByPeriodType(Period periodType);

  @Query("SELECT MAX(p.calculatedDate) FROM PopularBook p WHERE p.periodType = :periodType")
  Optional<LocalDate> findMaxCalculatedDateByPeriodType(@Param("periodType") Period periodType);
}

