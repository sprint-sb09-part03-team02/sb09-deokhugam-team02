package com.deokhugam.deokhugam_server.domain.review.event;

import java.util.UUID;

public record ReviewLikedEvent(
    UUID reviewId,      // 좋아요가 달린 리뷰 ID
    UUID likerId,       // 좋아요를 누른 사용자 ID
    String likerNickname,
    UUID targetUserId,  // 알림을 받을 리뷰 작성자 ID
    String reviewContent // 알림 메시지에 표시할 리뷰 내용 요약
) {}
