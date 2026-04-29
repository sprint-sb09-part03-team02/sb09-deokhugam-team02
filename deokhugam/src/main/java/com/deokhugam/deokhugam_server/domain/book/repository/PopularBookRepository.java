package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {

  List<PopularBook> findAllByPeriodTypeAndCalculatedDate(Period periodType, LocalDate calculatedDate);

  @Query("SELECT p FROM PopularBook p " +
    "JOIN FETCH p.book b " +
    "WHERE p.periodType = :period " +
    "AND (:after IS NULL OR p.createdAt < :after OR (p.createdAt = :after AND p.rankOrder > :cursor)) " +
    "ORDER BY p.createdAt DESC, p.rankOrder DESC")
  List<PopularBook> findPopularBooksDesc(
    @Param("period") Period period,
    @Param("cursor") Integer cursor,
    @Param("after") LocalDateTime after,
    Limit limit
  );

  @Query("SELECT p FROM PopularBook p " +
    "JOIN FETCH p.book b " +
    "WHERE p.periodType = :period " +
    "AND (:after IS NULL OR p.createdAt > :after OR (p.createdAt = :after AND p.rankOrder < :cursor)) " +
    "ORDER BY p.createdAt ASC, p.rankOrder ASC")
  List<PopularBook> findPopularBooksAsc(
    @Param("period") Period period,
    @Param("cursor") Integer cursor,
    @Param("after") LocalDateTime after,
    Limit limit
  );
  long countByPeriodType(Period periodType);

}

