package com.deokhugam.deokhugam_server.domain.review.service;

import static com.deokhugam.deokhugam_server.global.util.DateTimeUtils.parseLocalDateTime;

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
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewLikeRepository reviewLikeRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final ReviewMapper reviewMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final PopularReviewRepository popularReviewRepository;

  @Override
  @Transactional
  public ReviewDto createReview(ReviewCreateRequest request) {
    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND));

    if (reviewRepository.existsByBookIdAndUserIdAndIsDeletedFalse(request.bookId(), request.userId())) {
      throw new DeokhugamException(ErrorCode.ALREADY_REVIEWED);
    }

    Review review = reviewMapper.toEntity(request, book, user);
    Review savedReview = reviewRepository.save(review);

    return reviewMapper.toDto(savedReview, false);
  }

  @Override
  public ReviewDto getReview(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

    boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId);
    return reviewMapper.toDto(review, likedByMe);
  }

  @Override
  public CursorPageResponse<ReviewDto> searchReviews(ReviewSearchRequest request) {
    // 리포지토리에서 이미 ReviewDto 리스트를 반환함
    List<ReviewDto> content = reviewRepository.searchReviews(request);

    boolean hasNext = content.size() > request.getLimit();
    if (hasNext) {
      content.remove(content.size() - 1);
    }

    String nextCursor = null;
    LocalDateTime nextAfter = null;

    if (hasNext && !content.isEmpty()) {
      ReviewDto lastItem = content.get(content.size() - 1);
      nextCursor = lastItem.id().toString();
      nextAfter = lastItem.createdAt();
    }

    return new CursorPageResponse<>(content, nextCursor, nextAfter, request.getLimit(), 0L, hasNext);
  }

  @Override
  @Transactional
  public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

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

    if (!review.getUser().getId().equals(requestUserId)) {
      throw new DeokhugamException(ErrorCode.NOT_REVIEW_OWNER);
    }

    review.delete();
  }

  @Override
  @Transactional
  public void hardDeleteReview(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

    if (!review.getUser().getId().equals(requestUserId)) {
      throw new DeokhugamException(ErrorCode.NOT_REVIEW_OWNER);
    }

    reviewRepository.delete(review);
  }

  @Override
  @Transactional
  public ReviewLikeDto likeReview(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));
    User user = userRepository.findById(requestUserId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND));

    Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId);

    if (existingLike.isPresent()) {
      reviewLikeRepository.delete(existingLike.get());
      review.decreaseLikeCount();
      return new ReviewLikeDto(reviewId, requestUserId, false);
    } else {
      ReviewLike newLike = ReviewLike.builder()
          .review(review)
          .user(user)
          .build();
      reviewLikeRepository.save(newLike);
      review.increaseLikeCount();

      eventPublisher.publishEvent(new ReviewLikedEvent(
          reviewId, requestUserId, review.getUser().getId(), review.getContent()
      ));

      return new ReviewLikeDto(reviewId, requestUserId, true);
    }
  }

  @Override
  public CursorPageResponse<PopularReviewDto> searchPopularReviews(Period period, String direction, String cursor,
      String after, int limit) {
    Integer cursorRank = (cursor != null && !cursor.isBlank()) ? Integer.parseInt(cursor) : null;
    LocalDateTime afterLdt = parseLocalDateTime(after);

    List<PopularReview> popularReviews = popularReviewRepository.findPopularReviewsWithPaging(
        period, direction.toUpperCase(), cursorRank, afterLdt,
        Limit.of(limit + 1)
    );

    long totalElements = popularReviewRepository.countByPeriodType(period);

    boolean hasNext = popularReviews.size() > limit;
    List<PopularReview> content = hasNext ? popularReviews.subList(0, limit) : popularReviews;

    String nextCursor =
        content.isEmpty() ? null : String.valueOf(content.get(content.size() - 1).getRankOrder());
    LocalDateTime nextAfter =
        content.isEmpty() ? null : content.get(content.size() - 1).getCreatedAt();
    return new CursorPageResponse<>(
        content.stream().map(reviewMapper::toPopularDto).toList(),
        nextCursor, nextAfter, content.size(), totalElements, hasNext
    );
  }
}
