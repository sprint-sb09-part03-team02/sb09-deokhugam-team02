package com.deokhugam.deokhugam_server.domain.book.repository;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Slice;

public interface BookRepositoryCustom {

    Slice<Book> searchBooks(
            String keyword,
            String sortField,
            String sortDirection,
            String cursor,
            LocalDateTime after,
            int size
    );

    List<Book> findPopularBooks(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int limit
    );
}