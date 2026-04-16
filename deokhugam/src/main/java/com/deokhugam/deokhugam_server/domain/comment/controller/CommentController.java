package com.deokhugam.deokhugam_server.domain.comment.controller;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.service.CommentService;
import com.deokhugam.deokhugam_server.global.response.ApiResponse; // 우리는 일단 이거 쓰기로 했으니까!
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController { // 인터페이스 없이 바로 클래스 생성

  private final CommentService commentService;

  @PostMapping
  public ApiResponse<CommentDto> createComment(@Valid @RequestBody CommentCreateRequest request) {
    return ApiResponse.success(commentService.createComment(request), HttpStatus.CREATED);
  }

  @PatchMapping("/{commentId}")
  public ApiResponse<CommentDto> updateComment(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
      @Valid @RequestBody CommentUpdateRequest request) {
    return ApiResponse.success(commentService.updateComment(commentId, requestUserId, request));
  }

  @DeleteMapping("/{commentId}")
  public ApiResponse<Void> deleteComment(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    commentService.deleteComment(commentId, requestUserId);
    return ApiResponse.success(null, HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{commentId}/hard")
  public ApiResponse<Void> permanentDeleteComment(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    commentService.permanentDeleteComment(commentId, requestUserId);
    return ApiResponse.success(null, HttpStatus.NO_CONTENT);
  }

  @GetMapping("/{commentId}")
  public ApiResponse<CommentDto> getComment(@PathVariable UUID commentId) {
    return ApiResponse.success(commentService.getComment(commentId));
  }

  @GetMapping
  public ApiResponse<CursorPageResponse<CommentDto>> getCommentsByReviewId(@Valid CommentSearchRequest request) {
    return ApiResponse.success(commentService.getCommentsByReviewId(request));
  }
}