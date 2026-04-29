package com.deokhugam.deokhugam_server.domain.review.service;

import static com.deokhugam.deokhugam_server.domain.review.entity.QPopularReview.popularReview;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewLikeDto;
import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.entity.ReviewLike;
import com.deokhugam.deokhugam_server.domain.review.event.ReviewLikedEvent;
import com.deokhugam.deokhugam_server.domain.review.mapper.ReviewMapper;
import com.deokhugam.deokhugam_server.domain.review.repository.PopularReviewRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewLikeRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.mapper.StaticImagePathMapper;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

  @InjectMocks
  private ReviewServiceImpl reviewService;

  @Mock private ReviewRepository reviewRepository;
  @Mock private ReviewLikeRepository reviewLikeRepository;
  @Mock private BookRepository bookRepository;
  @Mock private UserRepository userRepository;
  @Mock private ReviewMapper reviewMapper;
  @Mock private PopularReviewRepository popularReviewRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private StaticImagePathMapper staticImagePathMapper;

  private User user;
  private Book book;
  private Review review;
  private final UUID userId = UUID.randomUUID();
  private final UUID bookId = UUID.randomUUID();
  private final UUID reviewId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    user = User.builder().id(userId).nickname("민주").build();
    book = new Book("제목", "저자", "123", "출판사", "설명", "url", LocalDate.now());
    ReflectionTestUtils.setField(book, "id", bookId);
    review = Review.builder()
      .id(reviewId)
      .user(user)
      .book(book)
      .content("꿀잼")
      .rating(5)
      .build();
  }

  @Nested
  @DisplayName("리뷰 생성 및 상세 조회")
  class ReviewReadWrite {

    @Test
    @DisplayName("생성 성공: 유효한 데이터로 리뷰 등록")
    void createReview_Success() {
      ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "꿀잼", 5);
      given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(reviewRepository.existsByBookIdAndUserIdAndIsDeletedFalse(bookId, userId)).willReturn(false);
      given(reviewMapper.toEntity(any(), any(), any())).willReturn(review);
      given(reviewRepository.save(any())).willReturn(review);
      given(reviewMapper.toDto(any(), anyBoolean())).willReturn(new ReviewDto(reviewId, bookId, "제목", "url", userId, "민주", "꿀잼", 5, 0, 0, false, null, null));

      ReviewDto result = reviewService.createReview(request);

      assertThat(result).isNotNull();
      assertThat(result.content()).isEqualTo("꿀잼");
      verify(reviewRepository).save(any());
    }

    @Test
    @DisplayName("생성 실패: 이미 리뷰를 작성한 도서 (1인 1리뷰 제한)")
    void createReview_Fail_AlreadyReviewed() {
      given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(reviewRepository.existsByBookIdAndUserIdAndIsDeletedFalse(bookId, userId)).willReturn(true);

      assertThatThrownBy(() -> reviewService.createReview(new ReviewCreateRequest(bookId, userId, "내용", 5)))
        .isInstanceOf(DeokhugamException.class)
        .hasMessageContaining(ErrorCode.ALREADY_REVIEWED.getMessage());
    }

    @Test
    @DisplayName("생성 실패: 도서를 찾을 수 없음")
    void createReview_Fail_BookNotFound() {
      given(bookRepository.findById(bookId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> reviewService.createReview(new ReviewCreateRequest(bookId, userId, "내용", 5)))
        .isInstanceOf(DeokhugamException.class)
        .hasMessageContaining(ErrorCode.BOOK_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상세 조회 성공: 좋아요 상태 포함")
    void getReview_Success() {
      given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
      given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(true);
      given(reviewMapper.toDto(any(), anyBoolean())).willReturn(new ReviewDto(reviewId, bookId, "제목", "url", userId, "민주", "꿀잼", 5, 0, 0, true, null, null));

      ReviewDto result = reviewService.getReview(reviewId, userId);
      assertThat(result.likedByMe()).isTrue();
    }

    @Test
    @DisplayName("조회 실패: 리뷰 없음")
    void getReview_Fail_NotFound() {
      given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());
      assertThatThrownBy(() -> reviewService.getReview(reviewId, userId))
        .isInstanceOf(DeokhugamException.class);
    }
  }

  @Nested
  @DisplayName("리뷰 목록 및 인기 리뷰 조회")
  class ReviewSearch {

    @Test
    @DisplayName("일반 검색: 페이징 로직(다음 페이지 있음) 검증")
    void searchReviews_Success_HasNext() {
      ReviewSearchRequest request = new ReviewSearchRequest();
      request.setLimit(1);
      List<ReviewDto> dtoList = new ArrayList<>(List.of(
        new ReviewDto(reviewId, bookId, "T1", "U1", userId, "N1", "C1", 5, 0, 0, false, LocalDateTime.now(), null),
        new ReviewDto(UUID.randomUUID(), bookId, "T2", "U2", userId, "N2", "C2", 4, 0, 0, false, LocalDateTime.now(), null)
      ));
      given(reviewRepository.searchReviews(any())).willReturn(dtoList);
      given(staticImagePathMapper.normalizeStaticImagePath(any())).willAnswer(invocation -> invocation.getArgument(0));

      CursorPageResponse<ReviewDto> result = reviewService.searchReviews(request);
      assertThat(result.content()).hasSize(1);
      assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("일반 검색: 도서 썸네일 URL을 응답용 경로로 변환")
    void searchReviews_NormalizeBookThumbnailUrl() {
      ReviewSearchRequest request = new ReviewSearchRequest();
      request.setLimit(10);
      String storedThumbnailUrl = "https://deokhugam-storage.s3.ap-northeast-2.amazonaws.com/thumbnails/book.jpg";
      String responseThumbnailUrl = "https://presigned.example.com/thumbnails/book.jpg";
      List<ReviewDto> dtoList = List.of(
          new ReviewDto(reviewId, bookId, "T1", storedThumbnailUrl, userId, "N1", "C1", 5, 0, 0, false, LocalDateTime.now(), null)
      );

      given(reviewRepository.searchReviews(any())).willReturn(dtoList);
      given(staticImagePathMapper.normalizeStaticImagePath(storedThumbnailUrl)).willReturn(responseThumbnailUrl);

      CursorPageResponse<ReviewDto> result = reviewService.searchReviews(request);

      assertThat(result.content()).hasSize(1);
      assertThat(result.content().get(0).bookThumbnailUrl()).isEqualTo(responseThumbnailUrl);
    }

    @Test
    @DisplayName("인기 리뷰 검색: 날짜 파싱 및 페이징 검증")
    void searchPopularReviews_Success() {
      PopularReview pr = PopularReview.builder().review(review).rankOrder(1).build();
      ReflectionTestUtils.setField(pr, "createdAt", LocalDateTime.now());

      when(popularReviewRepository.findPopularReviewDynamic(any(), any(), any(), anyString(), anyInt(), any(LocalDate.class)))
        .thenReturn(List.of(pr));
      given(popularReviewRepository.countByPeriodType(any())).willReturn(1L);

      CursorPageResponse<PopularReviewDto> result = reviewService.searchPopularReviews(Period.DAILY, "DESC", null, null, 10);
      assertThat(result.content()).isNotEmpty();
      verify(reviewMapper, atLeastOnce()).toPopularDto(any());
    }
  }

  @Nested
  @DisplayName("수정/삭제 권한 및 예외")
  class AuthorityAndExceptions {

    @Test
    @DisplayName("수정 실패: 작성자 아님")
    void updateReview_Fail_NotOwner() {
      User otherUser = User.builder().id(UUID.randomUUID()).build();
      Review otherReview = Review.builder().id(reviewId).user(otherUser).build();
      given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(otherReview));

      assertThatThrownBy(() -> reviewService.updateReview(reviewId, new ReviewUpdateRequest("내용", 5), userId))
        .isInstanceOf(DeokhugamException.class)
        .hasMessageContaining(ErrorCode.NOT_REVIEW_OWNER.getMessage());
    }

    @Test
    @DisplayName("물리 삭제 실패: 리뷰 존재 안 함")
    void hardDeleteReview_Fail_NotFound() {
      given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());
      assertThatThrownBy(() -> reviewService.hardDeleteReview(reviewId, userId))
        .isInstanceOf(DeokhugamException.class);
    }
  }

  @Nested
  @DisplayName("좋아요 토글 테스트")
  class LikeToggle {

    @Test
    @DisplayName("좋아요 추가 성공: 새 좋아요 저장 및 알림 이벤트 발행")
    void likeReview_Add_Success() {
      given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(Optional.empty());

      ReviewLikeDto result = reviewService.likeReview(reviewId, userId);

      assertThat(result.liked()).isTrue();
      verify(reviewLikeRepository).save(any());
      verify(eventPublisher).publishEvent(any(ReviewLikedEvent.class));
    }

    @Test
    @DisplayName("좋아요 취소: 이미 있는 경우 삭제 로직 실행")
    void likeReview_Cancel_Success() {
      ReviewLike existingLike = ReviewLike.builder().review(review).user(user).build();
      given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(Optional.of(existingLike));

      ReviewLikeDto result = reviewService.likeReview(reviewId, userId);

      assertThat(result.liked()).isFalse();
      verify(reviewLikeRepository).delete(existingLike);
    }

    @Test
    @DisplayName("좋아요 실패: 유저 없음")
    void likeReview_Fail_UserNotFound() {
      given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> reviewService.likeReview(reviewId, userId))
        .isInstanceOf(DeokhugamException.class);
    }
  }
}
