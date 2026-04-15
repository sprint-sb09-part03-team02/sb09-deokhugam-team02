package com.deokhugam.deokhugam_server.domain.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookCreateRequest {

    // 도서 제목은  공백 입력을 막음
    // DB 컬럼 길이와 맞추기 위해 최대 길이 제한.
    @NotBlank(message = "도서 제목은 필수입니다.")
    @Size(max = 255, message = "도서 제목은 255자 이하여야 합니다.")
    private String title;


    // 공백 입력을 막고 길이를 제한한다.
    @NotBlank(message = "저자는 필수입니다.")
    @Size(max = 100, message = "저자는 100자 이하여야 합니다.")
    private String author;

    // ISBN은 요구사항상 optional이다.
    // 너무 긴 값이 들어오지 않도록 길이만 제한한다.
    // 중복 여부는 Service 계층에서 검사한다.
    @Size(max = 20, message = "ISBN은 20자 이하여야 합니다.")
    private String isbn;

    // 출판사는 선택값으로 두되, DB 길이에 맞춰 길이를 제한한다.
    @Size(max = 100, message = "출판사는 100자 이하여야 합니다.")
    private String publisher;

    // 도서 소개는 선택값이다.
    // 너무 긴 문자열 저장을 막기 위해 최대 길이를 제한한다.
    @Size(max = 5000, message = "도서 설명은 5000자 이하여야 합니다.")
    private String description;


    // 등록 시점에 날짜가 빠지지 않도록 검증한다.
    @NotNull(message = "출간일은 필수입니다.")
    private LocalDate publishedAt;
}