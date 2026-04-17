package com.deokhugam.deokhugam_server.domain.book.controller;

import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.service.BookService;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;

  @GetMapping("/popular")
  public ResponseEntity<List<PopularBookDto>> searchPopularBooks(
      @RequestParam(defaultValue = "DAILY") Period period,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    List<PopularBookDto> popularBooks = bookService.searchPopularBooks(period, direction, cursor, after,
        limit);
    return ResponseEntity.ok(popularBooks);
  }
}