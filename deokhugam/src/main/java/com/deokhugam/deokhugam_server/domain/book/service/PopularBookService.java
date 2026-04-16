package com.deokhugam.deokhugam_server.domain.book.service;


import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.PopularBookDto;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.mapper.BookMapper;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.book.repository.PopularBookRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularBookService {

  private final BookRepository bookRepository;
  private final PopularBookRepository popularBookRepository;


  @Transactional
  public void calculateAndSaveRanks(Period periodType) {
    LocalDate endDate = LocalDate.now().minusDays(1);
    LocalDate startDate = getStartDate(periodType, endDate);

    List<BookRankQueryDto> statistics = bookRepository.findBookStatisticsForRanking(startDate, endDate);

    List<PopularBook> rankings = statistics.stream()
        .map(stat -> PopularBook.builder()
            .book(bookRepository.getReferenceById(stat.bookId()))
            .periodType(periodType)
            .score(stat.calculateScore())
            .reviewCount(stat.reviewCount())
            .rating(stat.avgRating())
            .calculatedDate(endDate)
            .build())
        .sorted(Comparator.comparing(PopularBook::getScore).reversed())
        .toList();

    for (int i = 0; i < rankings.size(); i++) {
      rankings.get(i).assignRankOrder(i + 1);
    }
    popularBookRepository.saveAll(rankings);
  }

  public List<PopularBookDto> getPopularBooks(Period periodType, LocalDate date) {
    return popularBookRepository.findAllByPeriodTypeAndCalculatedDate(periodType, date)
        .stream()
        .map(BookMapper::toPopularDto)
        .toList();
  }

  private LocalDate getStartDate(Period type, LocalDate endDate) {
    return switch (type) {
      case DAILY -> endDate;
      case WEEKLY -> endDate.minusWeeks(1);
      case MONTHLY -> endDate.minusMonths(1);
      case ALL_TIME -> LocalDate.of(2000, 1, 1);
      default -> throw new IllegalArgumentException("지원하지 않는 기간 타입입니다: " + type);
    };
  }
}