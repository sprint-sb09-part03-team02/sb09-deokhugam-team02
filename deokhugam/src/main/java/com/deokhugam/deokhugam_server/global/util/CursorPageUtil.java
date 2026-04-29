package com.deokhugam.deokhugam_server.global.util;

import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public class CursorPageUtil {

  /**
   * DB에서 가져온 리스트를 커서 페이지 응답 객체로 변환합니다.
   * * @param items          DB에서 조회한 결과 리스트 (보통 요청한 limit보다 +1해서 가져옴)
   * @param limit          클라이언트가 요청한 한 페이지 크기
   * @param totalElements  전체 데이터 개수
   * @param cursorExtractor 엔티티에서 ID(커서)를 뽑아내는 함수
   * @param timeExtractor   엔티티에서 시간(after)을 뽑아내는 함수
   */
  public static <T, R> CursorPageResponse<R> toResponse(
      List<T> items,
      int limit,
      long totalElements,
      Function<T, R> mapper,
      Function<T, String> cursorExtractor, // 커서값 추출 로직
      Function<T, LocalDateTime> timeExtractor // 시간값 추출 로직
  ) {
    // 1. 다음 페이지가 있는지 확인
    boolean hasNext = items.size() > limit;

    // 2. 실제 페이지 크기에 맞춰 데이터 자르기
    List<T> subList = hasNext ? items.subList(0, limit) : items;

    String nextCursor = null;
    LocalDateTime nextAfter = null;

    // 3. 다음 페이지가 있다면, 마지막 데이터에서 커서 정보 추출
    if (hasNext && !subList.isEmpty()) {
      T lastItem = subList.get(subList.size() - 1);
      nextCursor = cursorExtractor.apply(lastItem);
      nextAfter = timeExtractor.apply(lastItem);
    }

    // 4. Entity 리스트를 DTO 리스트로 변환
    List<R> content = subList.stream().map(mapper).toList();

    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }
}