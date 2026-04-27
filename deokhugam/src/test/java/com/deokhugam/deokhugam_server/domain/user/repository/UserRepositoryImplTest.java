package com.deokhugam.deokhugam_server.domain.user.repository;

import static com.deokhugam.deokhugam_server.domain.book.entity.QBook.book;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.entity.ReviewLike;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserRankQueryDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = "spring.flyway.enabled=false")
class UserRepositoryImplTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager em;

  @TestConfiguration
  @EnableJpaAuditing
  static class TestConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
      return new JPAQueryFactory(em);
    }
  }

  @Test
  @DisplayName("지정된 기간 내의 사용자 활동 통계를 정확히 조회한다")
  void findUserActivityStatisticsTest() {
    // 1. Given: 날짜 설정 (Auditing 반영을 위해 현재 시간 근처로 설정)
    LocalDate start = LocalDate.now().minusDays(1);
    LocalDate end = LocalDate.now().plusDays(1);

    // 사용자 생성
    User user1 = User.builder()
      .nickname("User1")
      .email("test@test.com")
      .password("1234password!")
      .build();
    em.persist(user1);

    Book book1 = new Book("테스트 도서 1", "작가1", "isbn-1", "출판사", "설명", "url", LocalDate.now());
    Book book2 = new Book("테스트 도서 2", "작가2", "isbn-2", "출판사", "설명", "url", LocalDate.now());
    em.persist(book1);
    em.persist(book2);

    // 기간 내 활동 생성
    Review r1 = Review.builder().user(user1).book(book1).content("Review 1").rating(5).build();
    Review r2 = Review.builder().user(user1).book(book2).content("Review 2").rating(4).build();
    em.persist(r1);
    em.persist(r2);

    ReviewLike like1 = ReviewLike.builder()
      .user(user1)
      .review(r1)
      .build();
    em.persist(like1);

    Comment c1 = Comment.builder()
      .userId(user1.getId())
      .review(r1)
      .content("Comment 1")
      .build();
    em.persist(c1);

    em.flush();
    em.clear();

    // 2. When
    List<UserRankQueryDto> results = userRepository.findUserActivityStatistics(start, end);

    // 3. Then
    assertThat(results).isNotEmpty();
    UserRankQueryDto dto = results.stream()
      .filter(r -> r.userId().equals(user1.getId()))
      .findFirst()
      .orElseThrow();

    assertThat(dto.totalReviewScore()).isEqualTo(2L);
    assertThat(dto.givenLikeCount()).isEqualTo(1L);
    assertThat(dto.writtenCommentCount()).isEqualTo(1L);
  }
}
