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

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private EntityManager em;

  private Book cleanCode;
  private Book effectiveJava;
  private Book noReviewBook;

  private User user1;
  private User user2;
  private User user3;

  private PopularBook rank1;

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

    persistReview(cleanCode, user1, "clean-review-1", 5);
    persistReview(cleanCode, user2, "clean-review-2", 3);
    persistReview(effectiveJava, user1, "java-review-1", 5);
    persistReview(effectiveJava, user3, "java-review-2", 5);

    rank1 = persistPopularBook(cleanCode, Period.DAILY, 10.0, 1, 2L, 4.0, RANK_DATE);
    persistPopularBook(effectiveJava, Period.DAILY, 9.0, 2, 2L, 5.0, RANK_DATE);
    persistPopularBook(noReviewBook, Period.DAILY, 1.0, 3, 0L, 0.0, RANK_DATE);

    em.flush();

    updateReviewCreatedAt("clean-review-1", BASE_TIME.minusDays(2));
    updateReviewCreatedAt("clean-review-2", BASE_TIME.minusDays(1));
    updateReviewCreatedAt("java-review-1", BASE_TIME.minusDays(2));
    updateReviewCreatedAt("java-review-2", BASE_TIME.minusDays(1));

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
  @DisplayName("성공: ISBN 검색 시 하이픈을 제거하고 검색한다")
  void searchBooks_KeywordIsbnHyphen_Success() {
    BookSearchRequest request = new BookSearchRequest(
      "978-2222222222",
      "title",
      "ASC",
      null,
      null,
      10
    );

    List<BookSearchQueryDto> result = bookRepository.searchBooks(request);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).title()).isEqualTo("Effective Java");
    assertThat(result.get(0).isbn()).isEqualTo("9782222222222");
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
  @DisplayName("성공: 제목 기준 내림차순으로 도서 목록을 조회한다")
  void searchBooks_OrderByTitleDesc_Success() {
    BookSearchRequest request = new BookSearchRequest(
      null,
      "title",
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
  @DisplayName("성공: 출간일 기준 오름차순으로 도서 목록을 조회한다")
  void searchBooks_OrderByPublishedDateAsc_Success() {
    BookSearchRequest request = new BookSearchRequest(
      null,
      "publishedDate",
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
  @DisplayName("성공: 평점 기준 오름차순으로 도서 목록을 조회한다")
  void searchBooks_OrderByRatingAsc_Success() {
    BookSearchRequest request = new BookSearchRequest(
      null,
      "rating",
      "ASC",
      null,
      null,
      10
    );

    List<BookSearchQueryDto> result = bookRepository.searchBooks(request);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).title()).isEqualTo("No Review Book");
    assertThat(result.get(0).rating()).isEqualTo(0.0);

    assertThat(result.get(1).title()).isEqualTo("Clean Code");
    assertThat(result.get(1).rating()).isEqualTo(4.0);

    assertThat(result.get(2).title()).isEqualTo("Effective Java");
    assertThat(result.get(2).rating()).isEqualTo(5.0);
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
  @DisplayName("성공: 리뷰 수 기준 오름차순으로 도서 목록을 조회한다")
  void searchBooks_OrderByReviewCountAsc_Success() {
    BookSearchRequest request = new BookSearchRequest(
      null,
      "reviewCount",
      "ASC",
      null,
      null,
      10
    );

    List<BookSearchQueryDto> result = bookRepository.searchBooks(request);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).title()).isEqualTo("No Review Book");
    assertThat(result.get(0).reviewCount()).isEqualTo(0);

    assertThat(result.get(1).reviewCount()).isEqualTo(2);
    assertThat(result.get(2).reviewCount()).isEqualTo(2);
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
  @DisplayName("성공: 제목 기준 커서 이후의 도서 목록을 조회한다")
  void searchBooks_TitleCursorPaging_Success() {
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
  @DisplayName("성공: 출간일 기준 커서 이후의 도서 목록을 조회한다")
  void searchBooks_PublishedDateCursorPaging_Success() {
    BookSearchRequest firstRequest = new BookSearchRequest(
      null,
      "publishedDate",
      "ASC",
      null,
      null,
      2
    );

    List<BookSearchQueryDto> firstResult = bookRepository.searchBooks(firstRequest);
    BookSearchQueryDto lastItem = firstResult.get(1);

    BookSearchRequest nextRequest = new BookSearchRequest(
      null,
      "publishedDate",
      "ASC",
      lastItem.publishedDate().toString(),
      lastItem.createdAt(),
      10
    );

    List<BookSearchQueryDto> nextResult = bookRepository.searchBooks(nextRequest);

    assertThat(nextResult).hasSize(1);
    assertThat(nextResult.get(0).title()).isEqualTo("No Review Book");
  }

  @Test
  @DisplayName("성공: 평점 기준 커서 이후의 도서 목록을 조회한다")
  void searchBooks_RatingCursorPaging_Success() {
    BookSearchRequest firstRequest = new BookSearchRequest(
      null,
      "rating",
      "ASC",
      null,
      null,
      2
    );

    List<BookSearchQueryDto> firstResult = bookRepository.searchBooks(firstRequest);
    BookSearchQueryDto lastItem = firstResult.get(1);

    BookSearchRequest nextRequest = new BookSearchRequest(
      null,
      "rating",
      "ASC",
      String.valueOf(lastItem.rating()),
      lastItem.createdAt(),
      10
    );

    List<BookSearchQueryDto> nextResult = bookRepository.searchBooks(nextRequest);

    assertThat(nextResult).hasSize(1);
    assertThat(nextResult.get(0).title()).isEqualTo("Effective Java");
  }

  @Test
  @DisplayName("성공: 리뷰 수 기준 커서 이후의 도서 목록을 조회한다")
  void searchBooks_ReviewCountCursorPaging_Success() {
    BookSearchRequest firstRequest = new BookSearchRequest(
      null,
      "reviewCount",
      "ASC",
      null,
      null,
      2
    );

    List<BookSearchQueryDto> firstResult = bookRepository.searchBooks(firstRequest);

    assertThat(firstResult).hasSize(3);

    BookSearchQueryDto lastItem = firstResult.get(1);

    BookSearchRequest nextRequest = new BookSearchRequest(
      null,
      "reviewCount",
      "ASC",
      String.valueOf(lastItem.reviewCount()),
      lastItem.createdAt(),
      10
    );

    List<BookSearchQueryDto> nextResult = bookRepository.searchBooks(nextRequest);

    assertThat(nextResult).isNotNull();
    assertThat(nextResult)
      .allSatisfy(book ->
        assertThat(book.reviewCount()).isGreaterThanOrEqualTo(lastItem.reviewCount())
      );
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
  @DisplayName("성공: 키워드 검색 결과의 도서 수를 조회한다")
  void countBooks_WithKeyword_Success() {
    long count = bookRepository.countBooks(new BookSearchRequest(
      "Java",
      "title",
      "ASC",
      null,
      null,
      10
    ));

    assertThat(count).isEqualTo(1);
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
  @DisplayName("성공: 존재하지 않는 도서 상세 조회 시 null을 반환한다")
  void findBookDetail_NotFound_ReturnsNull() {
    BookSearchQueryDto result = bookRepository.findBookDetail(UUID.randomUUID());

    assertThat(result).isNull();
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
  @DisplayName("성공: 기간 밖의 리뷰는 통계 집계에서 제외된다")
  void findBookStatisticsForRanking_OutOfRange_Excluded() {
    List<BookRankQueryDto> result = bookRepository.findBookStatisticsForRanking(
      BASE_TIME.plusDays(1).toLocalDate(),
      BASE_TIME.plusDays(2).toLocalDate()
    );

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("성공: 인기 도서 목록을 순위 기준 오름차순으로 조회한다")
  void findPopularBooksWithPaging_Asc_Success() {
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
  @DisplayName("성공: 인기 도서 목록을 DESC 방향으로 조회한다")
  void findPopularBooksWithPaging_Desc_Success() {
    List<PopularBook> result = bookRepository.findPopularBooksWithPaging(
      Period.DAILY,
      "DESC",
      null,
      null,
      10
    );

    assertThat(result).hasSize(3);

    assertThat(result)
      .extracting(PopularBook::getPeriodType)
      .containsOnly(Period.DAILY);

    assertThat(result)
      .extracting(PopularBook::getCalculatedDate)
      .containsOnly(RANK_DATE);

    assertThat(result)
      .extracting(PopularBook::getRankOrder)
      .containsExactlyInAnyOrder(1, 2, 3);
  }

  @Test
  @DisplayName("성공: 인기 도서 커서 페이징이 오름차순으로 정상 작동한다")
  void findPopularBooksWithPaging_AscCursor_Success() {
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

  @Test
  @DisplayName("성공: 인기 도서 조회 시 최신 집계일 데이터만 조회한다")
  void findPopularBooksWithPaging_LatestCalculatedDateOnly_Success() {
    persistPopularBook(
      cleanCode,
      Period.DAILY,
      100.0,
      99,
      10L,
      5.0,
      RANK_DATE.minusDays(1)
    );

    em.flush();
    em.clear();

    List<PopularBook> result = bookRepository.findPopularBooksWithPaging(
      Period.DAILY,
      "ASC",
      null,
      null,
      10
    );

    assertThat(result).hasSize(3);
    assertThat(result)
      .extracting(PopularBook::getCalculatedDate)
      .containsOnly(RANK_DATE);
  }

  @Test
  @DisplayName("성공: 인기 도서 조회 시 기간 타입이 일치하는 데이터만 조회한다")
  void findPopularBooksWithPaging_FilterByPeriod_Success() {
    persistPopularBook(
      cleanCode,
      Period.WEEKLY,
      100.0,
      1,
      10L,
      5.0,
      RANK_DATE
    );

    em.flush();
    em.clear();

    List<PopularBook> result = bookRepository.findPopularBooksWithPaging(
      Period.WEEKLY,
      "ASC",
      null,
      null,
      10
    );

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getPeriodType()).isEqualTo(Period.WEEKLY);
    assertThat(result.get(0).getRankOrder()).isEqualTo(1);
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
    int rating
  ) {
    Review review = Review.builder()
      .book(book)
      .user(user)
      .content(content)
      .rating(rating)
      .build();

    ReflectionTestUtils.setField(review, "createdAt", BASE_TIME);
    ReflectionTestUtils.setField(review, "updatedAt", BASE_TIME);

    em.persist(review);
    return review;
  }

  private void updateReviewCreatedAt(String content, LocalDateTime createdAt) {
    em.createNativeQuery("UPDATE reviews SET created_at = ?1, updated_at = ?1 WHERE content = ?2")
      .setParameter(1, createdAt)
      .setParameter(2, content)
      .executeUpdate();
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
