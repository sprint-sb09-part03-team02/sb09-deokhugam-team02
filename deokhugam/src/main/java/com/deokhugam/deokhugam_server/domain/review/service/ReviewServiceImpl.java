package com.deokhugam.deokhugam_server.domain.review.service;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.book.repository.BookRepository;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.mapper.ReviewMapper;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewLikeRepository;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewLikeRepository reviewLikeRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final ReviewMapper reviewMapper;

  @Override
  @Transactional
  public ReviewDto createReview(ReviewCreateRequest request) {
    // 1. 도서 및 사용자 객체 직접 조회 (참조를 위해 필요)
    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND));

    // 2. 1인 1리뷰 체크
    if (reviewRepository.existsByBookIdAndUserIdAndIsDeletedFalse(request.bookId(), request.userId())) {
      throw new DeokhugamException(ErrorCode.ALREADY_REVIEWED);
    }

    // 3. 리뷰 저장 (이제 매퍼에 book, user 객체를 직접 넘김)
    Review review = reviewMapper.toEntity(request, book, user);
    Review savedReview = reviewRepository.save(review);

    // 4. DTO 변환 (매퍼가 객체 내부를 타고 들어가므로 인자가 줄어듦!)
    return reviewMapper.toDto(savedReview, false);
  }

  @Override
  public ReviewDto getReview(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

    // 좋아요 여부 확인
    boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId);

    // 매퍼가 review.getBook().getTitle() 등을 자동으로 호출함
    return reviewMapper.toDto(review, likedByMe);
  }

  @Override
  public CursorPageResponse<ReviewDto> searchReviews(ReviewSearchRequest request) {
    List<Review> reviews = reviewRepository.searchReviews(request);

    boolean hasNext = reviews.size() > request.getLimit();
    if (hasNext) {
      reviews.remove(reviews.size() - 1);
    }

    // [혁신!] 이제 루프 돌면서 레포지토리 다시 뒤질 필요 없음 (객체 안에 다 있으니까)
    List<ReviewDto> content = reviews.stream().map(review -> {
      boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(review.getId(), request.getRequestUserId());
      return reviewMapper.toDto(review, likedByMe);
    }).collect(Collectors.toList());

    String nextCursor = hasNext ? reviews.get(reviews.size() - 1).getId().toString() : null;
    java.time.LocalDateTime nextAfter = hasNext ? reviews.get(reviews.size() - 1).getCreatedAt() : null;

    return new CursorPageResponse<>(content, nextCursor, nextAfter, request.getLimit(), 0L, hasNext);
  }

  @Override
  @Transactional
  public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

    // 본인 확인: review.getUser().getId()로 접근
    if (!review.getUser().getId().equals(requestUserId)) {
      throw new DeokhugamException(ErrorCode.NOT_REVIEW_OWNER);
    }

    review.update(request.content(), request.rating());

    boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId);
    return reviewMapper.toDto(review, likedByMe);
  }

  @Override
  @Transactional
  public void deleteReview(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

    // 본인 확인: review.getUser().getId()로 접근
    if (!review.getUser().getId().equals(requestUserId)) {
      throw new DeokhugamException(ErrorCode.NOT_REVIEW_OWNER);
    }

    review.delete();
  }

  @Override
  public List<ReviewDto> searchPopularReview(Period period, String direction, String cursor,
      String after, int limit) {
    LocalDateTime startTime = calculateStartTime(period);
    List<Review> popularReview = reviewRepository.findPopularReviewsWithPaging(
        startTime, direction, cursor, after, limit
    );
    return popularReview.stream()
        .map(r -> reviewMapper.toDto(r, false))
        .collect(Collectors.toList());
  }

  private LocalDateTime calculateStartTime(Period period) {
    return switch (period) {
      case DAILY -> LocalDateTime.now().minusDays(1);
      case WEEKLY -> LocalDateTime.now().minusWeeks(1);
      case MONTHLY -> LocalDateTime.now().minusMonths(1);
      case ALL_TIME -> LocalDateTime.of(2020, 1, 1, 0, 0); // 아주 오래전 시간
    };
  }
}