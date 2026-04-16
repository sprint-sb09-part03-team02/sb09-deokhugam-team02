package com.deokhugam.deokhugam_server.domain.book.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

public record BookSearchRequest(
        String keyword,
        String orderBy,
        String direction,
        String cursor,
        LocalDateTime after,

        @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
        @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
        Integer limit
) {

    public BookSearchRequest {
        if (orderBy == null || orderBy.isBlank()) {
            orderBy = "title";
        }

        if (direction == null || direction.isBlank()) {
            direction = "DESC";
        }

        if (limit == null) {
            limit = 10;
        }
    }
}