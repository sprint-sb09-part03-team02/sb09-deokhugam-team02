package com.deokhugam.deokhugam_server.domain.book.service;


import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.mapper.BookMapper;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.book.repository.PopularBookRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.global.util.PeriodUtil;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularBookService {

  private final BookRepository bookRepository;
  private final PopularBookRepository popularBookRepository;
  private final BookMapper bookMapper;

  @Transactional
  public void calculateAndSaveRanks(Period periodType) {
    LocalDate endDate = LocalDate.now().minusDays(1);
    LocalDate startDate = PeriodUtil.getStartDate(periodType, endDate);

    List<BookRankQueryDto> statistics = bookRepository.findBookStatisticsForRanking(startDate, endDate)
        .stream()
        .sorted(Comparator.comparingDouble(BookRankQueryDto::calculateScore).reversed())
        .toList();

    deleteExistingRankings(periodType, endDate);
    List<PopularBook> rankings = IntStream.range(0, statistics.size())
        .mapToObj(i -> createPopularBook(statistics.get(i), i + 1, periodType, endDate))
        .toList();

    popularBookRepository.saveAll(rankings);
}
  private void deleteExistingRankings(Period periodType, LocalDate date) {
    List<PopularBook> existing = popularBookRepository.findAllByPeriodTypeAndCalculatedDate(periodType, date);
    if (!existing.isEmpty()) {
      popularBookRepository.deleteAllInBatch(existing);
    }
  }

  private PopularBook createPopularBook(BookRankQueryDto dto, int rank, Period periodType, LocalDate date) {
    return PopularBook.builder()
        .book(bookRepository.getReferenceById(dto.bookId()))
        .periodType(periodType)
        .score(dto.calculateScore())
        .reviewCount(dto.reviewCount())
        .rating(dto.avgRating())
        .calculatedDate(date)
        .rankOrder(rank) // 빌더에서 바로 순위 할당
        .build();
  }
}