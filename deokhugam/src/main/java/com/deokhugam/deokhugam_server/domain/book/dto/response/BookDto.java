package com.deokhugam.deokhugam_server.domain.book.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookDto {

    // 도서 고유 식별자
    private UUID id;

    // 도서 제목
    private String title;

    // 저자명
    private String author;

    // ISBN
    private String isbn;

    // 출판사
    private String publisher;

    // 도서 소개
    private String description;

    // S3 등에 저장된 도서 이미지 URL
    private String imageUrl;

    // 출간일
    private LocalDate publishedAt;

    // 생성일시
    private LocalDateTime createdAt;

    // 수정일시
    private LocalDateTime updatedAt;

}