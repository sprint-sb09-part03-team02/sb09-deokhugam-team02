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
    // 1. 도서 및 사용자 존재 여부 확인
    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND));

    // 2. 도서 별 1개의 리뷰만 등록 가능 여부 체크
    if (reviewRepository.existsByBookIdAndUserIdAndIsDeletedFalse(request.bookId(), request.userId())) {
      throw new DeokhugamException(ErrorCode.ALREADY_REVIEWED); // ErrorCode 명칭 수정
    }

    // 3. 리뷰 저장
    Review review = reviewMapper.toEntity(request);
    Review savedReview = reviewRepository.save(review);

    return reviewMapper.toDto(savedReview, book.getTitle(), book.getImageUrl(), user.getNickname(), false);
  }

  @Override
  public ReviewDto getReview(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

    Book book = bookRepository.findById(review.getBookId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_NOT_FOUND));
    User user = userRepository.findById(review.getUserId())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.USER_NOT_FOUND));

    // 요청자의 좋아요 여부 포함
    boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId);

    return reviewMapper.toDto(review, book.getTitle(), book.getImageUrl(), user.getNickname(), likedByMe);
  }

  @Override
  public CursorPageResponse<ReviewDto> searchReviews(ReviewSearchRequest request) {
    // QueryDSL을 통한 목록 조회
    List<Review> reviews = reviewRepository.searchReviews(request);

    // 다음 페이지 존재 여부 확인 (limit+1 fetch 전략)
    boolean hasNext = reviews.size() > request.getLimit();
    if (hasNext) {
      reviews.remove(reviews.size() - 1);
    }

    List<ReviewDto> content = reviews.stream().map(review -> {
      Book book = bookRepository.findById(review.getBookId()).orElse(null);
      User user = userRepository.findById(review.getUserId()).orElse(null);
      boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(review.getId(), request.getRequestUserId());

      return reviewMapper.toDto(
          review,
          book != null ? book.getTitle() : "알 수 없는 도서",
          book != null ? book.getImageUrl() : null,
          user != null ? user.getNickname() : "알 수 없는 사용자",
          likedByMe
      );
    }).collect(Collectors.toList());

    // 커서 및 보조 커서 설정
    String nextCursor = hasNext ? reviews.get(reviews.size() - 1).getId().toString() : null;
    java.time.LocalDateTime nextAfter = hasNext ? reviews.get(reviews.size() - 1).getCreatedAt() : null;

    return new CursorPageResponse<>(content, nextCursor, nextAfter, request.getLimit(), 0L, hasNext);
  }

  @Override
  @Transactional
  public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

    // 본인이 작성한 리뷰만 수정 가능
    if (!review.getUserId().equals(requestUserId)) {
      throw new DeokhugamException(ErrorCode.NOT_REVIEW_OWNER);
    }

    review.update(request.content(), request.rating());

    // 수정 후 응답 데이터 구성
    Book book = bookRepository.findById(review.getBookId()).orElse(null);
    User user = userRepository.findById(review.getUserId()).orElse(null);
    boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId);

    return reviewMapper.toDto(review, book.getTitle(), book.getImageUrl(), user.getNickname(), likedByMe);
  }

  @Override
  @Transactional
  public void deleteReview(UUID reviewId, UUID requestUserId) {
    Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND));

    // 본인이 작성한 리뷰만 삭제 가능
    if (!review.getUserId().equals(requestUserId)) {
      throw new DeokhugamException(ErrorCode.NOT_REVIEW_OWNER); // ErrorCode 명칭 수정
    }

    // 논리 삭제 처리
    review.delete();
  }
}