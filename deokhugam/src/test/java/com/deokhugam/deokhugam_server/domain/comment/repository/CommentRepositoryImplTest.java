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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
public class CommentRepositoryImplTest {

  @Autowired private CommentRepository commentRepository;
  @Autowired private EntityManager em;

  private User user;
  private Review review;
  private UUID commentNewId;

  private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 4, 27, 10, 0, 0);
  private static final LocalDateTime FIXED_OLD = FIXED_NOW.minusDays(1);

  @BeforeEach
  void setUp() {
    // 1. 유저, 책, 리뷰 생성
    user = User.builder().email("test@test.com").nickname("민주").password("1234").build();
    em.persist(user);

    Book book = new Book("제목", "저자", "ISBN", "출판사", "설명", "url", LocalDate.now());
    em.persist(book);

    review = Review.builder().user(user).book(book).content("리뷰").rating(5).build();
    em.persist(review);
    em.flush();

    // 2. 옛날 댓글 생성
    Comment commentOld = Comment.builder().review(review).userId(user.getId()).content("옛날댓글").build();
    em.persist(commentOld);

    // 3. 최신 댓글 생성
    Comment commentNew = Comment.builder().review(review).userId(user.getId()).content("최신댓글").build();
    em.persist(commentNew);
    commentNewId = commentNew.getId();

    em.flush();

    em.createNativeQuery("UPDATE comments SET created_at = ?1 WHERE content = ?2")
      .setParameter(1, FIXED_OLD)
      .setParameter(2, "옛날댓글")
      .executeUpdate();

    em.createNativeQuery("UPDATE comments SET created_at = ?1 WHERE content = ?2")
      .setParameter(1, FIXED_NOW)
      .setParameter(2, "최신댓글")
      .executeUpdate();

    em.clear();
  }

  @Test
  @DisplayName("성공: 커서 페이징 로직이 정상 작동한다")
  void searchComments_Cursor_Success() {
    // given
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());
    request.setAfter(FIXED_NOW); // 최신댓글 시간 기준
    request.setCursor(commentNewId.toString());
    request.setDirection("DESC");
    request.setLimit(10);

    // when
    List<CommentDto> result = commentRepository.searchComments(request);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).content()).isEqualTo("옛날댓글");
  }

  @Test
  @DisplayName("성공: 오름차순(ASC) 정렬이 정상 작동한다")
  void searchComments_SortASC_Success() {
    // given
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());
    request.setDirection("ASC");
    request.setLimit(10);

    // when
    List<CommentDto> result = commentRepository.searchComments(request);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get(0).content()).isEqualTo("옛날댓글");
  }

  @Test
  @DisplayName("성공: 특정 리뷰의 댓글 목록을 조회하면 유저 닉네임이 포함되어야 한다")
  void searchComments_Join_Success() {
    // given
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());
    request.setLimit(10);

    // when
    List<CommentDto> result = commentRepository.searchComments(request);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.stream().anyMatch(c -> c.userNickname().equals("민주"))).isTrue();
  }

  @Test
  @DisplayName("성공: 삭제되지 않은 댓글 수만 카운트한다")
  void countComments_Success() {
    // when
    long count = commentRepository.countComments(review.getId());

    // then
    assertThat(count).isEqualTo(2);
  }
}
