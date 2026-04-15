package com.deokhugam.deokhugam_server.domain.book.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookUpdateRequest {

    // PATCH 요청이므로 모든 필드는 선택적으로 들어올 수 있다.
    // 다만 값이 들어왔을 때 너무 긴 문자열은 막아야 하므로 길이 제한만 둔다.
    @Size(max = 255, message = "도서 제목은 255자 이하여야 합니다.")
    private String title;

    // 저자도 수정 가능하지만, ISBN과 달리 선택적으로만 수정됨.
    @Size(max = 100, message = "저자는 100자 이하여야 합니다.")
    private String author;

    // 출판사 수정 시 길이 제한을 둔다.
    @Size(max = 100, message = "출판사는 100자 이하여야 합니다.")
    private String publisher;

    // 도서 소개 수정 시 길이 제한을 둔다.
    @Size(max = 5000, message = "도서 설명은 5000자 이하여야 합니다.")
    private String description;

    // 출간일도 수정 가능하다.
    private LocalDate publishedAt;




//     DTO 단계에서부터 수정 불가 정책을 넣어서 ISBN수정할 수 없게함 <  ISBN은 수정할 수 없음
}