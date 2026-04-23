package com.deokhugam.deokhugam_server.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.service.CommentService;
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
  @DisplayName("성공: 댓글 등록 시 HTTP 201을 반환하고 DTO를 직접 반환한다")
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
  @DisplayName("성공: 댓글 수정 시 HTTP 200 상태코드를 반환한다")
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
  @DisplayName("성공: 댓글 삭제 시 HTTP 204를 반환한다")
  void deleteComment_Success() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/{commentId}", commentId)
        .header(USER_HEADER, userId.toString()))
      .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("성공: 댓글 상세 조회")
  void getComment_Success() throws Exception {
    CommentDto response = CommentDto.builder().id(commentId).content("조회 내용").build();
    given(commentService.getComment(commentId)).willReturn(response);

    mockMvc.perform(get(BASE_URL + "/{commentId}", commentId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").value("조회 내용"));
  }
}
