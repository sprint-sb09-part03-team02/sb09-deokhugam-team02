package com.deokhugam.deokhugam_server.domain.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookCreateRequest(

  @NotBlank(message = "도서 제목은 필수입니다.")
  @Size(max = 255, message = "도서 제목은 255자 이하여야 합니다.")
  String title,

  @NotBlank(message = "저자는 필수입니다.")
  @Size(max = 100, message = "저자는 100자 이하여야 합니다.")
  String author,

  @NotBlank(message = "ISBN은 필수입니다.")
  @Size(min = 1, max = 20, message = "ISBN은 1자 이상 20자 이하여야 합니다.")
  String isbn,

  @Size(max = 100, message = "출판사는 100자 이하여야 합니다.")
  String publisher,

  @Size(max = 5000, message = "도서 설명은 5000자 이하여야 합니다.")
  String description,

  @NotNull(message = "출간일은 필수입니다.")
  LocalDate publishedDate
) {
}
