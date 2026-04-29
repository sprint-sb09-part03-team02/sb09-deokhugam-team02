package com.deokhugam.deokhugam_server.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewRankQueryDto;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.entity.ReviewLike;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Disabled;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
public class ReviewRepositoryImplTest {

  @Autowired private ReviewRepository reviewRepository;
  @Autowired private EntityManager em;

  private User user;
  private Book book1;
  private Review reviewNew;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

    user = User.builder().email("test@example.com").password("1234").nickname("테스터").build();
    ReflectionTestUtils.setField(user, "createdAt", now);
    ReflectionTestUtils.setField(user, "updatedAt", now);
    em.persist(user);

    book1 = new Book("자바의 정석", "저자1", "ISBN-001", "도우", "설명", "url", LocalDate.now());
    ReflectionTestUtils.setField(book1, "createdAt", now);
    ReflectionTestUtils.setField(book1, "updatedAt", now);
    em.persist(book1);

    reviewNew = Review.builder().user(user).book(book1).content("최신 리뷰").rating(5).build();
    ReflectionTestUtils.setField(reviewNew, "createdAt", now);
    ReflectionTestUtils.setField(reviewNew, "updatedAt", now);
    em.persist(reviewNew);

    Comment comment = Comment.builder().review(reviewNew).userId(user.getId()).content("댓글").build();
    ReflectionTestUtils.setField(comment, "createdAt", now);
    ReflectionTestUtils.setField(comment, "updatedAt", now);
    em.persist(comment);

    ReviewLike like = new ReviewLike(reviewNew, user);
    ReflectionTestUtils.setField(like, "createdAt", now);
    ReflectionTestUtils.setField(like, "updatedAt", now);
    em.persist(like);

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("성공: 확장된 검색 범위 검증 (도서 제목으로 검색)")
  void searchReviews_ByBookTitle() {
    ReviewSearchRequest request = new ReviewSearchRequest();
    request.setKeyword("자바");
    request.setLimit(10);
    request.setRequestUserId(user.getId());

    List<ReviewDto> result = reviewRepository.searchReviews(request);

    assertThat(result).isNotEmpty();
    assertThat(result.get(0).bookTitle()).contains("자바");
  }

  @Test
  @DisplayName("성공: 확장된 검색 범위 검증 (유저 닉네임으로 검색)")
  void searchReviews_ByUserNickname() {
    ReviewSearchRequest request = new ReviewSearchRequest();
    request.setKeyword("테스터");
    request.setLimit(10);
    request.setRequestUserId(user.getId());

    List<ReviewDto> result = reviewRepository.searchReviews(request);

    assertThat(result).isNotEmpty();
    assertThat(result.get(0).userNickname()).isEqualTo("테스터");
  }

  @Test
  @DisplayName("성공: 통계 쿼리에서 댓글과 좋아요 수가 정확히 집계된다")
  void findReviewStatistics_Accuracy() {
    List<ReviewRankQueryDto> stats = reviewRepository.findReviewStatistics(
      LocalDate.now().minusDays(1), LocalDate.now());

    assertThat(stats).anyMatch(s ->
      s.reviewId().equals(reviewNew.getId()) &&
        s.likeCount() == 1L &&
        s.commentCount() == 1L);
  }
}
