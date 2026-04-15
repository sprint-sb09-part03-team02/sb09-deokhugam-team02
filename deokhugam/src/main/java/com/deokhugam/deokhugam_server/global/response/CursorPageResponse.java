package com.deokhugam.deokhugam_server.global.response;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,        // 실제 데이터 리스트
    String nextCursor,      // 다음 조회를 위한 커서 ID
    LocalDateTime nextAfter, // 다음 조회를 위한 시간 기준점
    int size,               // 현재 페이지에 담긴 개수
    long totalElements,     // 전체 데이터 개수
    boolean hasNext         // 다음 페이지가 더 있는지 여부
) {}