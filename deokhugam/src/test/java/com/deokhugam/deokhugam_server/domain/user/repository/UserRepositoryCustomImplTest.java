package com.deokhugam.deokhugam_server.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import com.deokhugam.deokhugam_server.domain.comment.repository.CommentRepository;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.entity.ReviewLike;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewLikeRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserRankQueryDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
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
@Import(UserRepositoryCustomImplTest.TestQueryDslConfig.class)
@ActiveProfiles("test")
class UserRepositoryCustomImplTest {

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

  @Autowired private UserRepository userRepository;
  @Autowired private ReviewRepository reviewRepository;
  @Autowired private ReviewLikeRepository reviewLikeRepository;
  @Autowired private CommentRepository commentRepository;
  @Autowired private BookRepository bookRepository;
  @Autowired private EntityManager em;

  @Test
  @DisplayName("지정된 기간 내의 사용자 활동(리뷰, 좋아요, 댓글) 통계를 정확히 조회한다")
  void findUserActivityStatistics_Success() {
    // given
    LocalDate start = LocalDate.now().minusDays(1);
    LocalDate end = LocalDate.now();
    User activeUser = createUser("active@test.com", "활동유저");

    setupUserActivities(activeUser);

    em.flush();
    em.clear();

    // when
    List<UserRankQueryDto> result = userRepository.findUserActivityStatistics(start, end);

    // then
    assertThat(result).hasSize(1);

    assertThat(result.get(0))
      .extracting(
        UserRankQueryDto::userId,
        UserRankQueryDto::totalReviewScore,
        UserRankQueryDto::givenLikeCount,
        UserRankQueryDto::writtenCommentCount
      )
      .containsExactly(activeUser.getId(), 2L, 1L, 1L);
  }

  private void setupUserActivities(User user) {
    Book book1 = createBook("ISBN-111", "테스트 책 1");
    Review review1 = saveReview(user, book1, "리뷰1");

    Book book2 = createBook("ISBN-222", "테스트 책 2");
    saveReview(user, book2, "리뷰2");

    reviewLikeRepository.save(ReviewLike.builder()
      .user(user)
      .review(review1)
      .build());

    commentRepository.save(Comment.builder()
      .userId(user.getId())
      .review(review1)
      .content("댓글입니다")
      .build());
  }

  private User createUser(String email, String nickname) {
    return userRepository.save(User.builder()
      .email(email)
      .nickname(nickname)
      .password("password")
      .build());
  }

  private Book createBook(String isbn, String title) {
    return bookRepository.save(new Book(
      title, "저자", isbn, "출판사", "설명", "url", LocalDate.now()
    ));
  }

  private Review saveReview(User user, Book book, String content) {
    return reviewRepository.save(Review.builder()
      .user(user)
      .book(book)
      .content(content)
      .rating(5)
      .build());
  }
}
