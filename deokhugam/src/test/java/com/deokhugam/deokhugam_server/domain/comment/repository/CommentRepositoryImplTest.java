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
import org.springframework.test.util.ReflectionTestUtils;

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
    // 🔥 핵심: 나노초 단위를 0으로 맞춰서 DB와 Java의 시간 정밀도 차이를 없앰 (C++의 정밀도 보정 같은 작업)
    LocalDateTime now = LocalDateTime.now().withNano(0);

    // 1. 유저 생성 (createdAt, updatedAt 수동 주입)
    user = User.builder()
        .email("test@test.com")
        .nickname("민주")
        .password("1234")
        .build();
    ReflectionTestUtils.setField(user, "createdAt", now);
    ReflectionTestUtils.setField(user, "updatedAt", now);
    em.persist(user);

    // 2. 도서 생성
    Book book = new Book("제목", "저자", "ISBN", "출판사", "설명", "url", LocalDate.now());
    ReflectionTestUtils.setField(book, "createdAt", now);
    ReflectionTestUtils.setField(book, "updatedAt", now);
    em.persist(book);

    // 3. 리뷰 생성
    review = Review.builder().user(user).book(book).content("리뷰").rating(5).build();
    ReflectionTestUtils.setField(review, "createdAt", now);
    ReflectionTestUtils.setField(review, "updatedAt", now);
    em.persist(review);

    // 4. 댓글 생성 (시간차를 확실히 줌)
    commentOld = Comment.builder().reviewId(review.getId()).userId(user.getId()).content("옛날댓글").build();
    ReflectionTestUtils.setField(commentOld, "createdAt", now.minusHours(1)); // 1시간 전
    ReflectionTestUtils.setField(commentOld, "updatedAt", now.minusHours(1));
    em.persist(commentOld);

    commentNew = Comment.builder().reviewId(review.getId()).userId(user.getId()).content("최신댓글").build();
    ReflectionTestUtils.setField(commentNew, "createdAt", now); // 현재 시간
    ReflectionTestUtils.setField(commentNew, "updatedAt", now);
    em.persist(commentNew);

    em.flush();
    em.clear();
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
    assertThat(result.get(0).userNickname()).isEqualTo("민주"); // Join을 통한 닉네임 매핑 확인
    assertThat(result.get(0).content()).isEqualTo("최신댓글"); // 기본 DESC 정렬 확인
  }

  @Test
  @DisplayName("성공: 특정 유저가 쓴 댓글만 필터링한다")
  void searchComments_UserFilter_Success() {
    // given
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());
    request.setUserId(user.getId()); // 유저 필터링 추가
    request.setLimit(10);

    // when
    List<CommentDto> result = commentRepository.searchComments(request);

    // then
    assertThat(result).allMatch(c -> c.userId().equals(user.getId()));
  }

  @Test
  @DisplayName("성공: 커서 페이징(ltCursorAfter) 로직이 정상 작동한다")
  void searchComments_Cursor_Success() {
    // given: 최신 댓글 이후의 데이터를 요청
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());
    request.setAfter(commentNew.getCreatedAt()); // ltCursorAfter 분기 실행
    request.setCursor(commentNew.getId().toString());
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
    request.setDirection("ASC"); // ASC 정렬 분기 실행
    request.setLimit(10);

    // when
    List<CommentDto> result = commentRepository.searchComments(request);

    // then
    assertThat(result.get(0).content()).isEqualTo("옛날댓글");
  }

  @Test
  @DisplayName("성공: 삭제되지 않은 댓글 수만 카운트한다")
  void countComments_Success() {
    // when
    long count = commentRepository.countComments(review.getId());

    // then
    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("성공: 검색 조건이 없을 때 ltCursorAfter는 null을 반환하여 무시된다")
  void ltCursorAfter_Null_Success() {
    // given: after와 cursor가 없는 요청
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(review.getId());

    // when
    List<CommentDto> result = commentRepository.searchComments(request);

    // then
    assertThat(result).isNotEmpty(); // 에러 없이 조회가 되어야 함
  }
}