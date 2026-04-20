package com.deokhugam.deokhugam_server.domain.review.controller;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewSearchRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.service.ReviewService;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @Operation(summary = "리뷰 등록", description = "새로운 리뷰를 등록합니다.")
  @PostMapping
  public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewCreateRequest request) {
    ReviewDto response = reviewService.createReview(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "리뷰 상세 정보 조회", description = "리뷰 ID로 상세 정보를 조회합니다.")
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> getReview(
      @PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {

    ReviewDto response = reviewService.getReview(reviewId, requestUserId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "리뷰 목록 조회", description = "검색 조건에 맞는 리뷰 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<CursorPageResponse<ReviewDto>> searchReviews(
      @Valid ReviewSearchRequest request,
      @RequestHeader("Deokhugam-Request-User-ID") UUID headerUserId) {

    request.setRequestUserId(headerUserId);
    CursorPageResponse<ReviewDto> response = reviewService.searchReviews(request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> updateReview(
      @PathVariable UUID reviewId,
      @Valid @RequestBody ReviewUpdateRequest request,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {

    ReviewDto response = reviewService.updateReview(reviewId, request, requestUserId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "리뷰 논리 삭제", description = "본인이 작성한 리뷰를 논리적으로 삭제합니다.")
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {

    reviewService.deleteReview(reviewId, requestUserId);
    return ResponseEntity.noContent().build();
  }

  // 물리 삭제 엔드포인트 추가 (명세서 규격 반영)
  @Operation(summary = "리뷰 물리 삭제", description = "본인이 작성한 리뷰를 물리적으로 삭제합니다.")
  @DeleteMapping("/{reviewId}/hard")
  public ResponseEntity<Void> hardDeleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {

    reviewService.hardDeleteReview(reviewId, requestUserId);
    return ResponseEntity.noContent().build(); // 204 No Content
  }

  @Operation(summary = "인기 리뷰 목록 조회", description = "기간별 인기 리뷰 목록을 조회합니다.")
  @GetMapping("/popular")
  public ResponseEntity<List<PopularReviewDto>> searchPopularReview(
      @RequestParam(defaultValue = "DAILY") Period period,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    List<PopularReviewDto> popularReviews = reviewService.searchPopularReviews(period, direction, cursor, after, limit);
    return ResponseEntity.ok(popularReviews);
  }
}