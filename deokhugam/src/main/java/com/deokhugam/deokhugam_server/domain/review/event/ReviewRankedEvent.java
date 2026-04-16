package com.deokhugam.deokhugam_server.domain.review.event;

import com.deokhugam.deokhugam_server.global.type.Period;
import java.util.UUID;

/**
 * 리뷰가 인기 순위 상위권(10위 내)에 진입했을 때 발생하는 이벤트입니다.
 */
public record ReviewRankedEvent(
    UUID reviewId,
    UUID userId,        // 알림을 받을 리뷰 작성자 ID
    Period period,      // 일간, 주간, 월간 등 [cite: 169]
    int rank,           // 달성한 순위
    String reviewContent
) {}