package com.deokhugam.deokhugam_server.domain.book.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookUpdateRequest(

        @Size(max = 255, message = "도서 제목은 255자 이하여야 합니다.")
        String title,

        @Size(max = 100, message = "저자는 100자 이하여야 합니다.")
        String author,

        @Size(max = 100, message = "출판사는 100자 이하여야 합니다.")
        String publisher,

        @Size(max = 5000, message = "도서 설명은 5000자 이하여야 합니다.")
        String description,

        LocalDate publishedAt
) {
}