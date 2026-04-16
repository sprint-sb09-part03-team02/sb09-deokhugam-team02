package com.deokhugam.deokhugam_server.domain.review.batch;

import com.deokhugam.deokhugam_server.domain.review.service.PopularReviewService;
import com.deokhugam.deokhugam_server.global.type.Period;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularReviewBatch {
  private final PopularReviewService popularReviewService;

  public void execute(Period period) {
    popularReviewService.calculateAndSaveReviewRanks(period);
  }
}