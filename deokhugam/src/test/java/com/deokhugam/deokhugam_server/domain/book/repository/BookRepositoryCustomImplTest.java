package com.deokhugam.deokhugam_server.domain.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.deokhugam_server.domain.book.dto.request.BookSearchRequest;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookRankQueryDto;
import com.deokhugam.deokhugam_server.domain.book.dto.response.BookSearchQueryDto;
import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.entity.PopularBook;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.global.config.QueryDslConfig;
import com.deokhugam.deokhugam_server.global.type.Period;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class BookRepositoryCustomImplTest {

  @Autowired private BookRepository bookRepository;
  @Autowired private EntityManager em;

  private Book cleanCode;
  private Book effectiveJava;
  private Book noReviewBook;

  private User user1;
  private User user2;
  private User user3;

  private PopularBook rank1;
  private PopularBook rank2;
  private PopularBook rank3;

  private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 4, 27, 10, 0, 0);
  private static final LocalDate RANK_DATE = LocalDate.of(2026, 4, 27);

  @BeforeEach
  void setUp() {
    user1 = persistUser("user1");
    user2 = persistUser("user2");
    user3 = persistUser("user3");

    cleanCode = persistBook(
      "Clean Code",
      "Robert Martin",
      "9781111111111",
      LocalDate.of(2020, 1, 1),
      BASE_TIME.minusDays(3)
    );

    effectiveJava = persistBook(
      "Effective Java",
      "Joshua Bloch",
      "9782222222222",
      LocalDate.of(2021, 1, 1),
      BASE_TIME.minusDays(2)
    );

    noReviewBook = persistBook(
      "No Review Book",
      "Unknown",
      "9783333333333",
      LocalDate.of(2022, 1, 1),
      BASE_TIME.minusDays(1)
    );

    persistReview(cleanCode, user1, "좋은 책입니다.", 5, BASE_TIME.minusDays(2));
    persistReview(cleanCode, user2, "다시 읽고 싶은 책입니다.", 3, BASE_TIME.minusDays(1));

    persistReview(effectiveJava, user1, "자바 필독서입니다.", 5, BASE_TIME.minusDays(2));
    persistReview(effectiveJava, user3, "실무에 좋습니다.", 5, BASE_TIME.minusDays(1));

    rank1 = persistPopularBook(cleanCode, Period.DAILY, 10.0, 1, 2L, 4.0, RANK_DATE);
    rank2 = persistPopularBook(effectiveJava, Period.DAILY, 9.0, 2, 2L, 5.0, RANK_DATE);
    rank3 = persistPopularBook(noReviewBook, Period.DAILY, 1.0, 3, 0L, 0.0, RANK_DATE);

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("성공: 키워드로 제목, 저자, ISBN을 검색한다")
  void searchBooks_Keyword_Success() {
    BookSearchRequest titleRequest = new BookSearchRequest(
      "Clean",
      "title",
      "ASC",
      null,
      null,
      10
    );

    BookSearchRequest authorRequest = new BookSearchRequest(
      "Joshua",
      "title",
      "ASC",
      null,
      null,
      10
    );

    BookSearchRequest isbnRequest = new BookSearchRequest(
      "9782222222222",
      "title",
      "ASC",
      null,
      null,
      10
    );

    List<BookSearchQueryDto> titleResult = bookRepository.searchBooks(titleRequest);
    List<BookSearchQueryDto> authorResult = bookRepository.searchBooks(authorRequest);
    List<BookSearchQueryDto> isbnResult = bookRepository.searchBooks(isbnRequest);

    assertThat(titleResult).hasSize(1);
    assertThat(titleResult.get(0).title()).isEqualTo("Clean Code");

    assertThat(authorResult).hasSize(1);
    assertThat(authorResult.get(0).author()).isEqualTo("Joshua Bloch");

    assertThat(isbnResult).hasSize(1);
    assertThat(isbnResult.get(0).isbn()).isEqualTo("9782222222222");
  }

  @Test
  @DisplayName("성공: 제목 기준 오름차순으로 도서 목록을 조회한다")
  void searchBooks_OrderByTitleAsc_Success() {
    BookSearchRequest request = new BookSearchRequest(
      null,
      "title",
      "ASC",
      null,
      null,
      10
    );

    List<BookSearchQueryDto> result = bookRepository.searchBooks(request);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).title()).isEqualTo("Clean Code");
    assertThat(result.get(1).title()).isEqualTo("Effective Java");
    assertThat(result.get(2).title()).isEqualTo("No Review Book");
  }

  @Test
  @DisplayName("성공: 출간일 기준 내림차순으로 도서 목록을 조회한다")
  void searchBooks_OrderByPublishedDateDesc_Success() {
    BookSearchRequest request = new BookSearchRequest(
      null,
      "publishedDate",
      "DESC",
      null,
      null,
      10
    );

    List<BookSearchQueryDto> result = bookRepository.searchBooks(request);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).title()).isEqualTo("No Review Book");
    assertThat(result.get(1).title()).isEqualTo("Effective Java");
    assertThat(result.get(2).title()).isEqualTo("Clean Code");
  }

  @Test
  @DisplayName("성공: 평점 기준 내림차순으로 도서 목록을 조회한다")
  void searchBooks_OrderByRatingDesc_Success() {
    BookSearchRequest request = new BookSearchRequest(
      null,
      "rating",
      "DESC",
      null,
      null,
      10
    );

    List<BookSearchQueryDto> result = bookRepository.searchBooks(request);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).title()).isEqualTo("Effective Java");
    assertThat(result.get(0).rating()).isEqualTo(5.0);

    assertThat(result.get(1).title()).isEqualTo("Clean Code");
    assertThat(result.get(1).rating()).isEqualTo(4.0);

    assertThat(result.get(2).title()).isEqualTo("No Review Book");
    assertThat(result.get(2).rating()).isEqualTo(0.0);
  }

  @Test
  @DisplayName("성공: 리뷰 수 기준 내림차순으로 도서 목록을 조회한다")
  void searchBooks_OrderByReviewCountDesc_Success() {
    BookSearchRequest request = new BookSearchRequest(
      null,
      "reviewCount",
      "DESC",
      null,
      null,
      10
    );

    List<BookSearchQueryDto> result = bookRepository.searchBooks(request);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).reviewCount()).isEqualTo(2);
    assertThat(result.get(1).reviewCount()).isEqualTo(2);
    assertThat(result.get(2).reviewCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("성공: 커서 이후의 도서 목록을 조회한다")
  void searchBooks_CursorPaging_Success() {
    BookSearchRequest firstRequest = new BookSearchRequest(
      null,
      "title",
      "ASC",
      null,
      null,
      2
    );

    List<BookSearchQueryDto> firstResult = bookRepository.searchBooks(firstRequest);
    BookSearchQueryDto lastItem = firstResult.get(1);

    BookSearchRequest nextRequest = new BookSearchRequest(
      null,
      "title",
      "ASC",
      lastItem.title(),
      lastItem.createdAt(),
      10
    );

    List<BookSearchQueryDto> nextResult = bookRepository.searchBooks(nextRequest);

    assertThat(nextResult).hasSize(1);
    assertThat(nextResult.get(0).title()).isEqualTo("No Review Book");
  }

  @Test
  @DisplayName("성공: 삭제되지 않은 도서 수만 조회한다")
  void countBooks_Success() {
    long count = bookRepository.countBooks(new BookSearchRequest(
      null,
      "title",
      "ASC",
      null,
      null,
      10
    ));

    assertThat(count).isEqualTo(3);
  }

  @Test
  @DisplayName("성공: 도서 상세 조회 시 리뷰 수와 평균 평점을 함께 조회한다")
  void findBookDetail_Success() {
    BookSearchQueryDto result = bookRepository.findBookDetail(cleanCode.getId());

    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo("Clean Code");
    assertThat(result.reviewCount()).isEqualTo(2);
    assertThat(result.rating()).isEqualTo(4.0);
  }

  @Test
  @DisplayName("성공: 기간 내 리뷰 통계를 도서별로 집계한다")
  void findBookStatisticsForRanking_Success() {
    List<BookRankQueryDto> result = bookRepository.findBookStatisticsForRanking(
      BASE_TIME.minusDays(3).toLocalDate(),
      BASE_TIME.toLocalDate()
    );

    assertThat(result).hasSize(2);

    BookRankQueryDto cleanCodeStat = result.stream()
      .filter(dto -> dto.bookId().equals(cleanCode.getId()))
      .findFirst()
      .orElseThrow();

    BookRankQueryDto effectiveJavaStat = result.stream()
      .filter(dto -> dto.bookId().equals(effectiveJava.getId()))
      .findFirst()
      .orElseThrow();

    assertThat(cleanCodeStat.reviewCount()).isEqualTo(2);
    assertThat(cleanCodeStat.avgRating()).isEqualTo(4.0);

    assertThat(effectiveJavaStat.reviewCount()).isEqualTo(2);
    assertThat(effectiveJavaStat.avgRating()).isEqualTo(5.0);
  }

  @Test
  @DisplayName("성공: 인기 도서 목록을 순위 기준으로 조회한다")
  void findPopularBooksWithPaging_Success() {
    List<PopularBook> result = bookRepository.findPopularBooksWithPaging(
      Period.DAILY,
      "ASC",
      null,
      null,
      10
    );

    assertThat(result).hasSize(3);
    assertThat(result.get(0).getRankOrder()).isEqualTo(1);
    assertThat(result.get(1).getRankOrder()).isEqualTo(2);
    assertThat(result.get(2).getRankOrder()).isEqualTo(3);
  }

  @Test
  @DisplayName("성공: 인기 도서 커서 페이징이 정상 작동한다")
  void findPopularBooksWithPaging_Cursor_Success() {
    List<PopularBook> result = bookRepository.findPopularBooksWithPaging(
      Period.DAILY,
      "ASC",
      String.valueOf(rank1.getRankOrder()),
      rank1.getId().toString(),
      10
    );

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getRankOrder()).isEqualTo(2);
    assertThat(result.get(1).getRankOrder()).isEqualTo(3);
  }

  private User persistUser(String prefix) {
    String suffix = UUID.randomUUID().toString().substring(0, 8);

    User user = User.builder()
      .email(prefix + suffix + "@test.com")
      .nickname(prefix + suffix)
      .password("Password123!")
      .build();

    ReflectionTestUtils.setField(user, "createdAt", BASE_TIME);
    ReflectionTestUtils.setField(user, "updatedAt", BASE_TIME);

    em.persist(user);
    return user;
  }

  private Book persistBook(
    String title,
    String author,
    String isbn,
    LocalDate publishedDate,
    LocalDateTime createdAt
  ) {
    Book book = new Book(
      title,
      author,
      isbn,
      "출판사",
      "설명",
      "thumbnail.jpg",
      publishedDate
    );

    ReflectionTestUtils.setField(book, "createdAt", createdAt);
    ReflectionTestUtils.setField(book, "updatedAt", createdAt);

    em.persist(book);
    return book;
  }

  private Review persistReview(
    Book book,
    User user,
    String content,
    int rating,
    LocalDateTime createdAt
  ) {
    Review review = Review.builder()
      .book(book)
      .user(user)
      .content(content)
      .rating(rating)
      .build();

    ReflectionTestUtils.setField(review, "createdAt", createdAt);
    ReflectionTestUtils.setField(review, "updatedAt", createdAt);

    em.persist(review);
    return review;
  }

  private PopularBook persistPopularBook(
    Book book,
    Period period,
    double score,
    int rank,
    long reviewCount,
    double rating,
    LocalDate calculatedDate
  ) {
    PopularBook popularBook = PopularBook.builder()
      .book(book)
      .periodType(period)
      .score(score)
      .rankOrder(rank)
      .reviewCount(reviewCount)
      .rating(rating)
      .calculatedDate(calculatedDate)
      .build();

    ReflectionTestUtils.setField(popularBook, "createdAt", BASE_TIME.plusMinutes(rank));
    ReflectionTestUtils.setField(popularBook, "updatedAt", BASE_TIME.plusMinutes(rank));

    em.persist(popularBook);
    return popularBook;
  }
}
