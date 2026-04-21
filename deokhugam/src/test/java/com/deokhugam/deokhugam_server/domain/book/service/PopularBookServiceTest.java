package com.deokhugam.deokhugam_server.domain.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.book.repository.PopularBookRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PopularBookServiceTest {

  @InjectMocks
  private PopularBookService popularBookService;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private PopularBookRepository popularBookRepository;

  @Test
  @DisplayName("인기 도서 선정 성공")
  void calculateAndSaveRanks_Success() {
    // given
    // given
    Period period = Period.DAILY;
    LocalDate endDate = LocalDate.now().minusDays(1);

    // 1. UUID 타입에 맞춰서 생성
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    BookRankQueryDto dto1 = new BookRankQueryDto(id1, 10L, 4.5); // score: 6.7
    BookRankQueryDto dto2 = new BookRankQueryDto(id2, 20L, 4.0); // score: 10.4

    given(bookRepository.findBookStatisticsForRanking(any(), any()))
        .willReturn(List.of(dto1, dto2));

    // when
    popularBookService.calculateAndSaveRanks(period);

    // then
    ArgumentCaptor<List<PopularBook>> captor = ArgumentCaptor.forClass(List.class);

    verify(popularBookRepository).deleteAll(any());
    verify(popularBookRepository).saveAll(captor.capture());

    List<PopularBook> savedRankings = captor.getValue();

    // 검증 (AssertJ 사용 추천)
    assertThat(savedRankings).hasSize(2);
    assertThat(savedRankings.get(0).getRankOrder()).isEqualTo(1);
    assertThat(savedRankings.get(0).getBook().getId()).isEqualTo(id2); // 점수 높은 dto2가 1위
  }
}