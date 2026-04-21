package com.deokhugam.deokhugam_server.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
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
public class ReviewRepositoryImplTest {

  @Autowired private ReviewRepository reviewRepository;
  @Autowired private EntityManager em;

  private User user;
  private Book book1;
  private Book book2;
  private Review reviewOld;
  private Review reviewNew;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    // 1. User 생성: .id() 호출을 삭제해서 JPA가 ID를 자동 생성하게 함
    user = User.builder()
        .email("test@example.com")
        .password("1234")
        .nickname("테스터")
        .build();

    // 필수 필드인 시간 정보는 persist 전에 미리 주입
    ReflectionTestUtils.setField(user, "createdAt", now);
    ReflectionTestUtils.setField(user, "updatedAt", now);
    em.persist(user);

    // 2. Book 생성 (기존과 동일)
    book1 = new Book("자바의 정석 1", "저자1", "ISBN-001", "도우", "설명", "url", LocalDate.now());
    ReflectionTestUtils.setField(book1, "createdAt", now);
    ReflectionTestUtils.setField(book1, "updatedAt", now);
    em.persist(book1);

    book2 = new Book("자바의 정석 2", "저자2", "ISBN-002", "도우", "설명", "url", LocalDate.now());
    ReflectionTestUtils.setField(book2, "createdAt", now);
    ReflectionTestUtils.setField(book2, "updatedAt", now);
    em.persist(book2);

    // 3. Review 생성 (기존과 동일)
    reviewOld = Review.builder().user(user).book(book1).content("옛날 리뷰").rating(3).build();
    ReflectionTestUtils.setField(reviewOld, "createdAt", now.minusDays(1));
    ReflectionTestUtils.setField(reviewOld, "updatedAt", now.minusDays(1));
    em.persist(reviewOld);

    reviewNew = Review.builder().user(user).book(book2).content("최신 리뷰").rating(5).build();
    ReflectionTestUtils.setField(reviewNew, "createdAt", now);
    ReflectionTestUtils.setField(reviewNew, "updatedAt", now);
    em.persist(reviewNew);

    // 4. Like 추가 (기존과 동일)
    ReviewLike like = new ReviewLike(reviewNew, user);
    ReflectionTestUtils.setField(like, "createdAt", now);
    ReflectionTestUtils.setField(like, "updatedAt", now);
    em.persist(like);

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("성공: 키워드 검색 시 좋아요 여부와 데이터 매핑 확인")
  void searchReviews_Coverage() {
    ReviewSearchRequest request = new ReviewSearchRequest();
    request.setKeyword("최신");
    request.setRequestUserId(user.getId());
    request.setLimit(10);

    List<ReviewDto> result = reviewRepository.searchReviews(request);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).likedByMe()).isTrue();
  }

  @Test
  @DisplayName("성공: 커서 페이징(ltCursorAfter)의 모든 분기를 검증한다")
  void searchReviews_Cursor_Coverage() {
    ReviewSearchRequest request = new ReviewSearchRequest();
    request.setRequestUserId(user.getId());
    request.setAfter(reviewNew.getCreatedAt()); // ltCursorAfter 로직 실행
    request.setCursor(reviewNew.getId().toString());
    request.setLimit(10);

    List<ReviewDto> result = reviewRepository.searchReviews(request);

    assertThat(result).anyMatch(r -> r.id().equals(reviewOld.getId()));
  }

  @Test
  @DisplayName("성공: 통계 쿼리에서 날짜 범위 내의 데이터가 집계된다")
  void findReviewStatistics_Accuracy() {
    List<ReviewRankQueryDto> stats = reviewRepository.findReviewStatistics(
        LocalDate.now().minusDays(3), LocalDate.now());

    assertThat(stats).anyMatch(s -> s.reviewId().equals(reviewNew.getId()) && s.likeCount() == 1L);
  }
}