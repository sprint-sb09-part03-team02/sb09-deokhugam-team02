package com.deokhugam.deokhugam_server.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
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
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(PopularReviewRepositoryImplTest.TestQueryDslConfig.class)
@ActiveProfiles("test")
class PopularReviewRepositoryImplTest {

  @TestConfiguration
  @EnableJpaAuditing
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
  private EntityManager entityManager;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("인기 리뷰를 동적으로 조회하고 커서 기반 페이징이 적용된다")
  void findPopularReviewDynamic_Success() {
    // given
    User user = User.builder()
      .email("test@test.com")
      .nickname("테스터")
      .password("1234")
      .build();
    userRepository.save(user);
    LocalDate targetDate = LocalDate.of(2026, 4, 30);

    for (int i = 1; i <= 5; i++) {
      Book book = new Book(
        "테스트 책 " + i,      // title
        "작가 " + i,          // author
        "ISBN-000" + i,      // isbn (Unique 필드!)
        "데브 출판사",
        "설명",
        "http://image.com",
        LocalDate.now()
      );
    bookRepository.save(book);

      Review review = Review.builder()
        .content("리뷰 내용 " + i)
        .user(user)
        .book(book)
        .rating(5)
        .build();
      reviewRepository.save(review);

      PopularReview popularReview = PopularReview.builder()
        .review(review)
        .periodType(Period.WEEKLY)
        .rankOrder(i)
        .score(10.0)
        .calculatedDate(targetDate)
        .build();
      popularReviewRepository.save(popularReview);
    }

    entityManager.flush();
    entityManager.clear();

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
