package com.deokhugam.deokhugam_server.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
  private EntityManager entityManager;

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
  void findUserActivityStatistics_Success() {
    //Given
    LocalDate start = LocalDate.now().minusDays(1);
    LocalDate end = LocalDate.now().plusDays(1);

    User user = User.builder().nickname("User1").email("test@test.com").password("1234password!").build();
    entityManager.persist(user);

    Book book1 = new Book("테스트 도서 1", "작가1", "isbn-1", "출판사", "설명", "url", LocalDate.now());
    Book book2 = new Book("테스트 도서 2", "작가2", "isbn-2", "출판사", "설명", "url", LocalDate.now());
    entityManager.persist(book1);
    entityManager.persist(book2);

    Review r1 = Review.builder().user(user).book(book1).content("Review 1").rating(5).build();
    Review r2 = Review.builder().user(user).book(book2).content("Review 2").rating(4).build();
    entityManager.persist(r1);
    entityManager.persist(r2);

    ReviewLike like = ReviewLike.builder()
      .user(user)
      .review(r1)
      .build();
    entityManager.persist(like);

    Comment comment = Comment.builder()
      .userId(user.getId())
      .review(r1)
      .content("Comment 1")
      .build();
    entityManager.persist(comment);

    entityManager.flush();
    entityManager.clear();

    // When
    List<UserRankQueryDto> results = userRepository.findUserActivityStatistics(start, end);

    //Then
    assertThat(results).hasSize(1);

    UserRankQueryDto actual = results.get(0);
    assertThat(actual.userId()).isEqualTo(user.getId());
    assertThat(actual.totalReviewScore()).as("전체 리뷰 수는 2개여야 함").isEqualTo(2L);
    assertThat(actual.givenLikeCount()).as("전체 좋아요 수는 1개여야 함").isEqualTo(1L);
    assertThat(actual.writtenCommentCount()).as("전체 댓글 수는 1개여야 함").isEqualTo(1L);
  }
}
