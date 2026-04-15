package com.deokhugam.deokhugam_server.domain.book.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookSearchRequest {

    // 제목, 저자, ISBN 중 하나라도 부분 일치하는 도서를 검색할 때 사용하는 키워드다.
    // 값이 없으면 전체 목록 조회로 해석할 수 있다.
    private String keyword;


    private String sortField = "title";


    private String sortDirection = "asc";


    private String cursor;

    //이전 페이지 마지막 데이터의 생성 시간

    private LocalDateTime after;

    //한 번에 조회할 데이터 개수

    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
    private int size = 10;
}