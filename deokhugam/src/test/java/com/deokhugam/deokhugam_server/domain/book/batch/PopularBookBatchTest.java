package com.deokhugam.deokhugam_server.domain.book.batch;

import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.book.service.PopularBookService;
import com.deokhugam.deokhugam_server.global.type.Period;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PopularBookBatchTest {

  @InjectMocks
  private PopularBookBatch popularBookBatch;

  @Mock
  private PopularBookService popularBookService;

  @Test
  @DisplayName("성공: 데일리 배치 실행 시 도서 랭킹 서비스의 DAILY 산출 로직을 호출한다")
  void executeDaily_Success() {
    // when
    popularBookBatch.executeDaily();

    // then
    verify(popularBookService).calculateAndSaveRanks(Period.DAILY);
  }
}
