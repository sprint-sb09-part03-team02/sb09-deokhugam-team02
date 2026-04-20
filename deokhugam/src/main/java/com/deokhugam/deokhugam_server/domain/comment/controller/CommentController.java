package com.deokhugam.deokhugam_server.domain.comment.controller;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.service.CommentService;
import com.deokhugam.deokhugam_server.global.response.ApiResponse;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// 1. 태그를 달아줘야 스웨거에서 "댓글 관리"로 묶임!
@Tag(name = "댓글 관리", description = "댓글 관련 API")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
  @PostMapping
  public ApiResponse<CommentDto> createComment(@Valid @RequestBody CommentCreateRequest request) {
    return ApiResponse.success(commentService.createComment(request), HttpStatus.CREATED);
  }

  @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
  @PatchMapping("/{commentId}")
  public ApiResponse<CommentDto> updateComment(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
      @Valid @RequestBody CommentUpdateRequest request) {
    return ApiResponse.success(commentService.updateComment(commentId, requestUserId, request));
  }

  @Operation(summary = "댓글 논리 삭제", description = "본인이 작성한 댓글을 논리적으로 삭제합니다.")
  @DeleteMapping("/{commentId}")
  public ApiResponse<Void> deleteComment(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    commentService.deleteComment(commentId, requestUserId);
    return ApiResponse.success(null, HttpStatus.NO_CONTENT);
  }

  @Operation(summary = "댓글 물리 삭제", description = "본인이 작성한 댓글을 물리적으로 삭제합니다.")
  @DeleteMapping("/{commentId}/hard")
  public ApiResponse<Void> permanentDeleteComment(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    commentService.permanentDeleteComment(commentId, requestUserId);
    return ApiResponse.success(null, HttpStatus.NO_CONTENT);
  }

  @Operation(summary = "댓글 상세 정보 조회", description = "특정 댓글의 상세 정보를 조회합니다.")
  @GetMapping("/{commentId}")
  public ApiResponse<CommentDto> getComment(@PathVariable UUID commentId) {
    return ApiResponse.success(commentService.getComment(commentId));
  }

  @Operation(summary = "리뷰 댓글 목록 조회", description = "특정 리뷰에 달린 댓글 목록을 시간순으로 조회합니다.")
  @GetMapping
  public ApiResponse<CursorPageResponse<CommentDto>> getCommentsByReviewId(@Valid CommentSearchRequest request) {
    return ApiResponse.success(commentService.getCommentsByReviewId(request));
  }
}