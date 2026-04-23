package com.deokhugam.deokhugam_server.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewLikeDto; // 추가됨
import com.deokhugam.deokhugam_server.domain.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ReviewService reviewService;

  private final UUID userId = UUID.randomUUID();
  private final UUID bookId = UUID.randomUUID();
  private final UUID reviewId = UUID.randomUUID();

  private final String BASE_URL = "/api/reviews";
  private final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

  @Test
  @DisplayName("성공: 리뷰 생성 (201 Created 반환 및 DTO 직접 검증)")
  void createReview_Success() throws Exception {
    ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "정말 좋아요", 5);

    ReviewDto response = new ReviewDto(
      reviewId, bookId, "자바의 정석", "url",
      userId, "테스터", "정말 좋아요", 5,
      0, 0, false, null, null
    );

    given(reviewService.createReview(any())).willReturn(response);

    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.content").value("정말 좋아요"));
  }

  @Test
  @DisplayName("성공: 리뷰 상세 조회 (ResponseEntity 반영)")
  void getReview_Success() throws Exception {
    ReviewDto response = new ReviewDto(reviewId, bookId, "제목", "url", userId, "닉네임", "내용", 5, 0, 0, false, null, null);
    given(reviewService.getReview(eq(reviewId), eq(userId))).willReturn(response);

    mockMvc.perform(get(BASE_URL + "/{reviewId}", reviewId)
        .header(USER_ID_HEADER, userId.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").value("내용"));
  }

  // --- 추가됨: 리뷰 좋아요 테스트 ---
  @Test
  @DisplayName("성공: 리뷰 좋아요 추가/취소 (200 OK)")
  void likeReview_Success() throws Exception {
    // response의 필드명을 확인하세요.
    // 만약 ReviewLikeDto에 boolean liked; 라고 되어 있다면 JSON은 "liked"로 나옵니다.
    ReviewLikeDto response = new ReviewLikeDto(reviewId, userId, true);
    given(reviewService.likeReview(eq(reviewId), eq(userId))).willReturn(response);

    mockMvc.perform(post(BASE_URL + "/{reviewId}/like", reviewId)
        .header(USER_ID_HEADER, userId.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.liked").value(true)); // 💡 $.isLiked에서 $.liked로 수정!
  }
  // ----------------------------

  @Test
  @DisplayName("성공: 인기 리뷰 조회 (복수형 메서드명 반영)")
  void searchPopularReview_Success() throws Exception {
    mockMvc.perform(get(BASE_URL + "/popular")
        .param("period", "DAILY")
        .param("direction", "DESC")) // 기본값 DESC 반영
      .andExpect(status().isOk());
  }

  @Test
  @DisplayName("성공: 리뷰 삭제 (204 No Content)")
  void deleteReview_Success() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/{reviewId}", reviewId)
        .header(USER_ID_HEADER, userId.toString()))
      .andExpect(status().isNoContent());
  }
}
