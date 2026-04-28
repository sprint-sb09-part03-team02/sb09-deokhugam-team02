package com.deokhugam.deokhugam_server.domain.book.batch;

import com.deokhugam.deokhugam_server.domain.book.service.PopularBookService;
import com.deokhugam.deokhugam_server.global.type.Period;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularBookBatch {
  private final PopularBookService popularBookService;

  public void executeDaily() {
    execute(Period.DAILY);
  }

  public void execute(Period period) {
    popularBookService.calculateAndSaveRanks(period);
  }
}
