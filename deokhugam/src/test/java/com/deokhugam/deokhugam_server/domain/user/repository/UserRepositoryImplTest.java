package com.deokhugam.deokhugam_server.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

@DataJpaTest
class UserRepositoryImplTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager em;

  @TestConfiguration
  static class TestConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
      return new JPAQueryFactory(em);
    }
  }

  @Test
  @DisplayName("지정된 기간 내의 사용자 활동 통계를 정확히 조회한다")
  void findUserActivityStatisticsTest() {
    LocalDate start = LocalDate.of(2024, 1, 1);
    LocalDate end = LocalDate.of(2024, 1, 31);

    User user1 = User.builder().nickname("User1").build();
    em.persist(user1);

    // 기간 내 활동 생성 (리뷰 2, 좋아요 1, 댓글 1)
    Review r1 = Review.builder().user(user1).content("Review 1").build(); // createdAt 자동생성 가정
    Review r2 = Review.builder().user(user1).content("Review 2").build();
    em.persist(r1);
    em.persist(r2);

    ReviewLike like1 = ReviewLike.builder().user(user1).review(r1).build();
    em.persist(like1);

    Comment c1 = Comment.builder().userId(user1.getId()).content("Comment 1").build();
    em.persist(c1);

    // 2. When
    List<UserRankQueryDto> results = userRepository.findUserActivityStatistics(start, end);
    // 3. Then
    assertThat(results).isNotEmpty();
    UserRankQueryDto dto = results.stream()
      .filter(r -> r.userId().equals(user1.getId())) // record는 userId()로 접근
      .findFirst()
      .orElseThrow();

    assertThat(dto.totalReviewScore()).isEqualTo(2.0); // count가 Double로 매핑될 경우
    assertThat(dto.givenLikeCount()).isEqualTo(1L);
    assertThat(dto.writtenCommentCount()).isEqualTo(1L);
  }
}
