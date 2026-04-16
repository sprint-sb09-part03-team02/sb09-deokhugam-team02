package com.deokhugam.deokhugam_server.domain.book.dto.response;

import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDateTime;
import java.util.UUID;

public record PopularBookDto(
        UUID id,
        UUID bookId,
        String title,
        String author,
        String thumbnailUrl,
        Period period,
        int rank,
        double score,
        int reviewCount,
        double rating,
        LocalDateTime createdAt
) {
}