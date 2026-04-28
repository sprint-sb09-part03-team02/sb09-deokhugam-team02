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

  @Query("SELECT p FROM PopularBook p "
      + "JOIN FETCH p.book b " +
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
  List<PopularBook> findPopularBooksWithPaging(
      @Param("period") Period period,
      @Param("direction") String direction,
      @Param("cursor") Integer cursor,
      @Param("after") LocalDateTime after,
      @Param("limit") Limit limit
  );

  long countByPeriodType(Period periodType);

}

