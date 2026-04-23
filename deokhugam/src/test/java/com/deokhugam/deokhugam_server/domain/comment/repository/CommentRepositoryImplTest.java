package com.deokhugam.deokhugam_server.domain.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

@Disabled("H2 DB에서 PostgreSQL uuid-ossp 확장 기능 미지원으로 인한 임시 비활성화")
@DataJpaTest
@Import(QueryDslConfig.class)
public class CommentRepositoryImplTest {

  @Autowired private CommentRepository commentRepository;
  @Autowired private EntityManager em;

  private User user;
  private Review review;
  private Comment commentOld;
  private Comment commentNew;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

    user = User.builder().email("test@test.com").nickname("민주").password("1234").build();
    ReflectionTestUtils.setField(user, "createdAt", now);
    ReflectionTestUtils.setField(user, "updatedAt", now);
    em.persist(user);

    Book book = new Book("제목", "저자", "ISBN", "출판사", "설명", "url", LocalDate.now());
    ReflectionTestUtils.setField(book, "createdAt", now);
    ReflectionTestUtils.setField(book, "updatedAt", now);
    em.persist(book);

    review = Review.builder().user(user).book(book).content("리뷰").rating(5).build();
    ReflectionTestUtils.setField(review, "createdAt", now);
    ReflectionTestUtils.setField(review, "updatedAt", now);
    em.persist(review);

    // 💡 수정됨: reviewId(UUID) 대신 review(객체)를 직접 주입
    commentOld = Comment.builder()
      .review(review) // 변경된 부분
      .userId(user.getId())
      .content("옛날댓글")
      .build();
    ReflectionTestUtils.setField(commentOld, "createdAt", now.minusDays(1));
    ReflectionTestUtils.setField(commentOld, "updatedAt", now.minusDays(1));
    em.persist(commentOld);

    commentNew = Comment.builder()
      .review(review) // 변경된 부분
      .userId(user.getId())
      .content("최신댓글")
      .build();
    ReflectionTestUtils.setField(commentNew, "createdAt", now);
    ReflectionTestUtils.setField(commentNew, "updatedAt", now);
    em.persist(commentNew);

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("성공: 특정 리뷰의 댓글 목록을 조회하면 유저 닉네임이 포함되어야 한다")
  void searchComments_Join_Success() {
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());
    request.setLimit(10);

    List<CommentDto> result = commentRepository.searchComments(request);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).userNickname()).isEqualTo("민주");
    assertThat(result.get(0).content()).isEqualTo("최신댓글");
  }

  @Test
  @DisplayName("성공: 특정 유저가 쓴 댓글만 필터링한다")
  void searchComments_UserFilter_Success() {
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());
    request.setUserId(user.getId());
    request.setLimit(10);

    List<CommentDto> result = commentRepository.searchComments(request);

    assertThat(result).allMatch(c -> c.userId().equals(user.getId()));
  }

  @Test
  @DisplayName("성공: 커서 페이징(ltCursorAfter) 로직이 정상 작동한다")
  void searchComments_Cursor_Success() {
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());
    request.setAfter(commentNew.getCreatedAt());
    request.setCursor(commentNew.getId().toString());
    request.setLimit(10);

    List<CommentDto> result = commentRepository.searchComments(request);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).content()).isEqualTo("옛날댓글");
  }

  @Test
  @DisplayName("성공: 오름차순(ASC) 정렬이 정상 작동한다")
  void searchComments_SortASC_Success() {
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());
    request.setDirection("ASC");
    request.setLimit(10);

    List<CommentDto> result = commentRepository.searchComments(request);

    assertThat(result.get(0).content()).isEqualTo("옛날댓글");
  }

  @Test
  @DisplayName("성공: 삭제되지 않은 댓글 수만 카운트한다")
  void countComments_Success() {
    long count = commentRepository.countComments(review.getId());
    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("성공: 검색 조건이 없을 때 ltCursorAfter는 null을 반환하여 무시된다")
  void ltCursorAfter_Null_Success() {
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());

    List<CommentDto> result = commentRepository.searchComments(request);

    assertThat(result).isNotEmpty();
  }
}
