package com.deokhugam.deokhugam_server.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.service.CommentService;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
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

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CommentService commentService;

  private final String BASE_URL = "/api/comments";
  private final String USER_HEADER = "Deokhugam-Request-User-ID";
  private final UUID userId = UUID.randomUUID();
  private final UUID reviewId = UUID.randomUUID();
  private final UUID commentId = UUID.randomUUID();

  @Test
  @DisplayName("성공: 댓글 등록")
  void createComment_Success() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "댓글 내용");
    CommentDto response = CommentDto.builder().content("댓글 내용").build();
    given(commentService.createComment(any())).willReturn(response);

    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.content").value("댓글 내용"));
  }

  @Test
  @DisplayName("성공: 리뷰별 댓글 목록 조회 (커서 페이징)")
  void getCommentsByReviewId_Success() throws Exception {
    CommentDto dto = CommentDto.builder().content("댓글").build();

    java.util.List<CommentDto> list = new java.util.ArrayList<>();
    list.add(dto);

    CursorPageResponse<CommentDto> response = new CursorPageResponse<>(
      list,           // content
      null,           // nextCursor
      null,           // nextAfter
      list.size(),    // size
      1L,             // totalElements (테스트용 임의 값)
      false           // hasNext
    );

    given(commentService.getCommentsByReviewId(any())).willReturn(response);

    mockMvc.perform(get(BASE_URL)
        .param("reviewId", reviewId.toString())
        .param("limit", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].content").value("댓글"));
  }

  @Test
  @DisplayName("성공: 댓글 상세 정보 조회")
  void getComment_Success() throws Exception {
    CommentDto response = CommentDto.builder().id(commentId).content("조회 내용").build();
    given(commentService.getComment(commentId)).willReturn(response);

    mockMvc.perform(get(BASE_URL + "/{commentId}", commentId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").value("조회 내용"));
  }

  @Test
  @DisplayName("성공: 댓글 수정")
  void updateComment_Success() throws Exception {
    CommentUpdateRequest request = new CommentUpdateRequest("수정 내용");
    CommentDto response = CommentDto.builder().content("수정 내용").build();
    given(commentService.updateComment(any(), any(), any())).willReturn(response);

    mockMvc.perform(patch(BASE_URL + "/{commentId}", commentId)
        .header(USER_HEADER, userId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").value("수정 내용"));
  }

  @Test
  @DisplayName("성공: 댓글 논리 삭제")
  void deleteComment_Success() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/{commentId}", commentId)
        .header(USER_HEADER, userId.toString()))
      .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("성공: 댓글 물리 삭제")
  void permanentDeleteComment_Success() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/{commentId}/hard", commentId)
        .header(USER_HEADER, userId.toString()))
      .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("실패: 댓글 등록 시 내용이 없으면 HTTP 400")
  void createComment_Validation_Fail() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "");

    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest());
  }
}
