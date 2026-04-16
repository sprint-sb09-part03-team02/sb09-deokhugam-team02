package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom {

    Optional<Book> findByIdAndIsDeletedFalse(UUID id);

    Optional<Book> findByIsbnAndIsDeletedFalse(String isbn);

    boolean existsByIdAndIsDeletedFalse(UUID id);

    boolean existsByIsbnAndIsDeletedFalse(String isbn);
}