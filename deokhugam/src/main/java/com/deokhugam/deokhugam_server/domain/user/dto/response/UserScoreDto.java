package com.deokhugam.deokhugam_server.domain.user.dto.response;

import java.util.UUID;

public record UserScoreDto(UUID userId, Double totalScore) {
}
