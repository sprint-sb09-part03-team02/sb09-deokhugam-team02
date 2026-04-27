package com.deokhugam.deokhugam_server.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewLikeDto;
import com.deokhugam.deokhugam_server.domain.review.service.ReviewService;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

  @Nested
  @DisplayName("리뷰 생성 테스트")
  class CreateReview {

    @Test
    @DisplayName("성공: 리뷰 생성 시 201 Created 반환")
    void createReview_Success() throws Exception {
      ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "최고의 책입니다.", 5);
      ReviewDto response = new ReviewDto(reviewId, bookId, "제목", "url", userId, "민주", "최고의 책입니다.", 5, 0, 0, false, LocalDateTime.now(), null);

      given(reviewService.createReview(any())).willReturn(response);

      mockMvc.perform(post(BASE_URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("최고의 책입니다."));
    }

    @Test
    @DisplayName("실패: 평점이 범위를 벗어나면 400 에러")
    void createReview_Fail_Rating() throws Exception {
      ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "내용", 6);
      mockMvc.perform(post(BASE_URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("리뷰 조회 및 검색 테스트")
  class ReviewSearch {

    @Test
    @DisplayName("성공: 리뷰 목록 검색 (페이징 반영)")
    void searchReviews_Success() throws Exception {
      ReviewDto dto = new ReviewDto(reviewId, bookId, "자바의 정석", "url", userId, "민주", "내용", 5, 0, 0, false, LocalDateTime.now(), null);
      // CursorPageResponse 생성자 인자 6개 (content, nextCursor, nextAfter, size, totalElements, hasNext)
      CursorPageResponse<ReviewDto> response = new CursorPageResponse<>(List.of(dto), null, null, 1, 1L, false);

      given(reviewService.searchReviews(any())).willReturn(response);

      mockMvc.perform(get(BASE_URL)
          .header(USER_ID_HEADER, userId.toString())
          .param("bookId", bookId.toString())
          .param("requestUserId", userId.toString())
          .param("limit", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].bookTitle").value("자바의 정석"));
    }

    @Test
    @DisplayName("성공: 리뷰 상세 조회")
    void getReview_Success() throws Exception {
      ReviewDto response = new ReviewDto(reviewId, bookId, "제목", "url", userId, "민주", "내용", 5, 0, 0, false, LocalDateTime.now(), null);
      given(reviewService.getReview(eq(reviewId), eq(userId))).willReturn(response);

      mockMvc.perform(get(BASE_URL + "/{reviewId}", reviewId)
          .header(USER_ID_HEADER, userId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("내용"));
    }

    @Test
    @DisplayName("성공: 인기 리뷰 조회")
    void searchPopularReview_Success() throws Exception {
      // PopularReviewDto 인자 맞춰서 생성
      PopularReviewDto dto = new PopularReviewDto(UUID.randomUUID(), reviewId, bookId, "인기 도서", "url", userId, "민주", "내용", 5.0, Period.DAILY, LocalDateTime.now(), 1, 100.0, 10, 5);
      CursorPageResponse<PopularReviewDto> response = new CursorPageResponse<>(List.of(dto), null, null, 1, 1L, false);

      given(reviewService.searchPopularReviews(any(), anyString(), any(), any(), anyInt())).willReturn(response);

      mockMvc.perform(get(BASE_URL + "/popular")
          .param("period", "DAILY")
          .param("direction", "DESC"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].bookTitle").value("인기 도서"));
    }
  }

  @Nested
  @DisplayName("수정 및 삭제 테스트")
  class UpdateDelete {

    @Test
    @DisplayName("성공: 리뷰 수정 시 200 OK")
    void updateReview_Success() throws Exception {
      ReviewUpdateRequest request = new ReviewUpdateRequest("수정 내용", 4);
      ReviewDto response = new ReviewDto(reviewId, bookId, "제목", "url", userId, "민주", "수정 내용", 4, 0, 0, false, LocalDateTime.now(), null);

      given(reviewService.updateReview(eq(reviewId), any(), eq(userId))).willReturn(response);

      mockMvc.perform(patch(BASE_URL + "/{reviewId}", reviewId)
          .header(USER_ID_HEADER, userId.toString())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정 내용"));
    }

    @Test
    @DisplayName("성공: 리뷰 삭제 시 204 No Content")
    void deleteReview_Success() throws Exception {
      mockMvc.perform(delete(BASE_URL + "/{reviewId}", reviewId)
          .header(USER_ID_HEADER, userId.toString()))
        .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("좋아요 테스트")
  class LikeReview {
    @Test
    @DisplayName("성공: 리뷰 좋아요 토글")
    void likeReview_Success() throws Exception {
      ReviewLikeDto response = new ReviewLikeDto(reviewId, userId, true);
      given(reviewService.likeReview(eq(reviewId), eq(userId))).willReturn(response);

      mockMvc.perform(post(BASE_URL + "/{reviewId}/like", reviewId)
          .header(USER_ID_HEADER, userId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(true));
    }
  }
}
