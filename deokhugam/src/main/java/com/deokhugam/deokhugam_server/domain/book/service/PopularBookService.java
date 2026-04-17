package com.deokhugam.deokhugam_server.domain.book.service;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.mapper.PopularBookMapper;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.book.repository.PopularBookRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularBookService {

  private static final int DEFAULT_LIMIT = 10;

  private final BookRepository bookRepository;
  private final PopularBookRepository popularBookRepository;
  private final PopularBookMapper popularBookMapper;

  /**
   * 특정 기간의 인기 도서를 배치 방식으로 계산해서 저장한다.
   */
  @Transactional
  public void calculateAndSavePopularBooks(Period period, LocalDate calculatedDate) {
    LocalDate startDate = resolveStartDate(period, calculatedDate);
    LocalDate endDate = calculatedDate;

    List<BookRankQueryDto> statistics =
            bookRepository.findBookStatisticsForRanking(startDate, endDate);

    List<PopularBook> popularBooks = statistics.stream()
            .map(stat -> {
              Book book = bookRepository.findByIdAndIsDeletedFalse(stat.bookId())
                      .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));

              long reviewCount = stat.reviewCount() == null ? 0L : stat.reviewCount();
              double rating = stat.avgRating() == null ? 0.0 : stat.avgRating();

              return new PopularBook(
                      book,
                      period,
                      stat.calculateScore(),
                      0,
                      calculatedDate,
                      reviewCount,
                      rating
              );
            })
            .sorted(Comparator.comparing((PopularBook popularBook) -> popularBook.getScore()).reversed())
            .limit(DEFAULT_LIMIT)
            .toList();

    for (int i = 0; i < popularBooks.size(); i++) {
      popularBooks.get(i).assignRankOrder(i + 1);
    }

    List<PopularBook> existingRanks =
            popularBookRepository.findAllByPeriodTypeAndCalculatedDate(period, calculatedDate);

    if (!existingRanks.isEmpty()) {
      popularBookRepository.deleteAll(existingRanks);
    }

    popularBookRepository.saveAll(popularBooks);
  }

  /**
   * 특정 기간/날짜 기준 인기 도서 목록 조회
   */
  public List<PopularBookDto> getPopularBooks(Period period, LocalDate calculatedDate) {
    List<PopularBook> popularBooks =
            popularBookRepository.findAllByPeriodTypeAndCalculatedDate(period, calculatedDate);

    List<PopularBookDto> result = new ArrayList<>();

    for (PopularBook popularBook : popularBooks) {
      int reviewCount = popularBook.getReviewCount() == null
              ? 0
              : popularBook.getReviewCount().intValue();

      double rating = popularBook.getRating() == null
              ? 0.0
              : popularBook.getRating();

      result.add(popularBookMapper.toDto(popularBook, reviewCount, rating));
    }

    return result;
  }

  private LocalDate resolveStartDate(Period period, LocalDate endDate) {
    return switch (period) {
      case DAILY -> endDate.minusDays(1);
      case WEEKLY -> endDate.minusWeeks(1);
      case MONTHLY -> endDate.minusMonths(1);
      case ALL_TIME -> LocalDate.of(2000, 1, 1);
    };
  }
}