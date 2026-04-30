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
import org.junit.jupiter.api.BeforeEach;
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

  private User testUser;
  private final LocalDate targetDate = LocalDate.of(2026, 4, 30);

  @BeforeEach
  void setUp() {
    testUser = User.builder()
      .email("test@test.com")
      .nickname("테스터")
      .password("1234")
      .build();
    userRepository.save(testUser);
  }

  @Test
  @DisplayName("인기 리뷰 조회: ASC 정렬 및 커서 기반 페이징이 정확히 작동한다")
  void findPopularReviewDynamic_Pagination_Success() {
    for (int i = 1; i <= 5; i++) {
      createPopularReviewSet(i);
    }

    entityManager.flush();
    entityManager.clear();

    int cursor = 2;
    int limit = 2;
    List<PopularReview> result = popularReviewRepository.findPopularReviewDynamic(
      Period.WEEKLY, cursor, null, "ASC", limit, targetDate
    );

    assertThat(result).hasSize(limit + 1)
      .extracting(PopularReview::getRankOrder)
      .containsExactly(3, 4, 5);

    assertThat(result.get(0).getReview().getContent()).contains("리뷰 내용 3");
  }


  private void createPopularReviewSet(int index) {
    Book book = new Book(
      "테스트 책 " + index, "작가 " + index, "ISBN-000" + index,
      "데브 출판사", "설명", "http://image.com", LocalDate.now()
    );
    bookRepository.save(book);

    Review review = Review.builder()
      .content("리뷰 내용 " + index)
      .user(testUser)
      .book(book)
      .rating(5)
      .build();
    reviewRepository.save(review);

    PopularReview popularReview = PopularReview.builder()
      .review(review)
      .periodType(Period.WEEKLY)
      .rankOrder(index)
      .score(10.0)
      .calculatedDate(targetDate)
      .build();
    popularReviewRepository.save(popularReview);
  }
}
