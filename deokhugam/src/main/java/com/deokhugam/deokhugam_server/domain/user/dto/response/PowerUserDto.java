package com.deokhugam.deokhugam_server.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PowerUserDto(
    UUID userId,
    String nickname,
    Period period,
    LocalDateTime createdAt,
    int rank,
    double score,
    double reviewScoreSum,
    int likeCount,
    int commentCount
) {

}
