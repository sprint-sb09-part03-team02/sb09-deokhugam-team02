package com.deokhugam.deokhugam_server.domain.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.book.repository.PopularBookRepository;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

  @Captor
  private ArgumentCaptor<List<PopularBook>> listCaptor;

  @Test
  @DisplayName("인기 도서 선정 성공")
  void calculateAndSaveRanks_Success() {
    // given
    Period period = Period.DAILY;

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    BookRankQueryDto lowScoreDto = new BookRankQueryDto(id1, 10L, 3.5);
    BookRankQueryDto highScoreDto = new BookRankQueryDto(id2, 50L, 4.8);

    given(bookRepository.findBookStatisticsForRanking(any(), any()))
        .willReturn(List.of(lowScoreDto, highScoreDto));

    Book mockBook1 = mock(Book.class);
    Book mockBook2 = mock(Book.class);
    given(mockBook1.getId()).willReturn(id1);
    given(mockBook2.getId()).willReturn(id2);

    given(bookRepository.getReferenceById(id1)).willReturn(mockBook1);
    given(bookRepository.getReferenceById(id2)).willReturn(mockBook2);

    given(popularBookRepository.findAllByPeriodTypeAndCalculatedDate(any(), any()))
        .willReturn(List.of());

    // when
    popularBookService.calculateAndSaveRanks(period);

    // then
    verify(popularBookRepository).saveAll(listCaptor.capture());
    List<PopularBook> result = listCaptor.getValue();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getBook().getId()).isEqualTo(id2);
    assertThat(result.get(1).getBook().getId()).isEqualTo(id1);
  }
}