package com.deokhugam.deokhugam_server.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CursorPageUtilTest {

  @Test
  @DisplayName("성공: limit보다 결과가 많으면 다음 커서와 after를 반환한다")
  void toResponse_HasNext() {
    LocalDateTime firstTime = LocalDateTime.of(2026, 4, 28, 10, 0);
    LocalDateTime secondTime = LocalDateTime.of(2026, 4, 28, 9, 0);
    List<TestItem> items = List.of(
      new TestItem("first", firstTime),
      new TestItem("second", secondTime),
      new TestItem("third", LocalDateTime.of(2026, 4, 28, 8, 0))
    );

    CursorPageResponse<String> response = CursorPageUtil.toResponse(
      items,
      2,
      3,
      TestItem::id,
      TestItem::id,
      TestItem::createdAt
    );

    assertThat(response.content()).containsExactly("first", "second");
    assertThat(response.nextCursor()).isEqualTo("second");
    assertThat(response.nextAfter()).isEqualTo(secondTime);
    assertThat(response.size()).isEqualTo(2);
    assertThat(response.totalElements()).isEqualTo(3);
    assertThat(response.hasNext()).isTrue();
  }

  @Test
  @DisplayName("성공: 다음 페이지가 없으면 커서와 after를 null로 반환한다")
  void toResponse_NoNext() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 4, 28, 10, 0);
    List<TestItem> items = List.of(new TestItem("only", createdAt));

    CursorPageResponse<String> response = CursorPageUtil.toResponse(
      items,
      20,
      1,
      TestItem::id,
      TestItem::id,
      TestItem::createdAt
    );

    assertThat(response.content()).containsExactly("only");
    assertThat(response.nextCursor()).isNull();
    assertThat(response.nextAfter()).isNull();
    assertThat(response.hasNext()).isFalse();
  }

  private record TestItem(String id, LocalDateTime createdAt) {
  }
}
