package com.deokhugam.deokhugam_server.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(PopularReviewRepositoryImplTest.TestQueryDslConfig.class)
@ActiveProfiles("test")
class PopularReviewRepositoryImplTest {

  @TestConfiguration
  static class TestQueryDslConfig {
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
      return new JPAQueryFactory(entityManager);
    }
  }

  @Autowired
  private PopularReviewRepository popularReviewRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private EntityManager em;

  @Test
  @DisplayName("인기 리뷰를 동적으로 조회하고 커서 기반 페이징이 적용된다")
  void findPopularReviewDynamic_Success() {
    // given
    LocalDate targetDate = LocalDate.of(2026, 4, 30); // 현재 시점 기준 날짜

    for (int i = 1; i <= 5; i++) {
      Review review = Review.builder().content("리뷰 " + i).build();
      reviewRepository.save(review);

      PopularReview popularReview = PopularReview.builder()
        .review(review)
        .periodType(Period.WEEKLY)
        .rankOrder(i)
        .calculatedDate(targetDate)
        .build();
      popularReviewRepository.save(popularReview);
    }

    em.flush();
    em.clear();

    // when
    List<PopularReview> result = popularReviewRepository.findPopularReviewDynamic(
      Period.WEEKLY,
      2,
      null,
      "ASC",
      2,
      targetDate
    );

    // then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getRankOrder()).isEqualTo(3);
    assertThat(result.get(1).getRankOrder()).isEqualTo(4);
    assertThat(result.get(0).getReview().getContent()).contains("리뷰");
  }
}
