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
    User inactiveUser = createUser("inactive@test.com", "무활동유저");

    Book book = new Book("제목", "저자", "ISBN-111", "출판사", "설명", "url", LocalDate.now());
    bookRepository.save(book);

    saveReview(activeUser, book, "리뷰1");
    saveReview(activeUser, book, "리뷰2");

    Review reviewForLike = reviewRepository.findAll().get(0);
    reviewLikeRepository.save(ReviewLike.builder().user(activeUser).review(reviewForLike).build());

    commentRepository.save(Comment.builder()
      .userId(activeUser.getId())
      .review(reviewForLike)
      .content("댓글입니다")
      .build());

    em.flush();
    em.clear();

    // when
    List<UserRankQueryDto> result = userRepository.findUserActivityStatistics(start, end);

    // then
    assertThat(result).hasSize(1);

    UserRankQueryDto stats = result.get(0);
    assertThat(stats.userId()).isEqualTo(activeUser.getId());
    assertThat(stats.totalReviewScore()).isEqualTo(2L);
    assertThat(stats.givenLikeCount()).isEqualTo(1L);
    assertThat(stats.writtenCommentCount()).isEqualTo(1L);
  }

  private User createUser(String email, String nickname) {
    return userRepository.save(User.builder()
      .email(email)
      .nickname(nickname)
      .password("password")
      .build());
  }

  private void saveReview(User user, Book book, String content) {
    reviewRepository.save(Review.builder()
      .user(user)
      .book(book)
      .content(content)
      .rating(5)
      .build());
  }
}
