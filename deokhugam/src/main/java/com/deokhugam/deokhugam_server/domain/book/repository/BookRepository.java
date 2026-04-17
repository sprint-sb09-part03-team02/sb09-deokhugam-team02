package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom {

    Optional<Book> findByIdAndIsDeletedFalse(UUID id);

    Optional<Book> findByIsbnAndIsDeletedFalse(String isbn);

    boolean existsByIsbnAndIsDeletedFalse(String isbn);

    @Query("SELECT b FROM Book b WHERE b.createdAt >= :startTime AND b.isDeleted = false")
    List<PopularBook> findPopularBooksWithPaging(
        @Param("startTime") LocalDateTime startTime,
        @Param("direction") String direction,
        @Param("cursor") String cursor,
        @Param("after") String after,
        @Param("limit") int limit
    );
}