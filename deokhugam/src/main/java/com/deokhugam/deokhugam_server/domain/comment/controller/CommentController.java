package com.deokhugam.deokhugam_server.domain.comment.controller;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.service.CommentService;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "댓글 관리", description = "댓글 관련 API")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
  @PostMapping
  public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentCreateRequest request) {
    CommentDto response = commentService.createComment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> updateComment(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
      @Valid @RequestBody CommentUpdateRequest request) {
    CommentDto response = commentService.updateComment(commentId, requestUserId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "댓글 논리 삭제", description = "본인이 작성한 댓글을 논리적으로 삭제합니다.")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    commentService.deleteComment(commentId, requestUserId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "댓글 물리 삭제", description = "본인이 작성한 댓글을 물리적으로 삭제합니다.")
  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> permanentDeleteComment(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    commentService.permanentDeleteComment(commentId, requestUserId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "댓글 상세 정보 조회", description = "특정 댓글의 상세 정보를 조회합니다.")
  @GetMapping("/{commentId}")
  public ResponseEntity<CommentDto> getComment(@PathVariable UUID commentId) {
    CommentDto response = commentService.getComment(commentId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "리뷰 댓글 목록 조회", description = "특정 리뷰에 달린 댓글 목록을 시간순으로 조회합니다.")
  @GetMapping
  public ResponseEntity<CursorPageResponse<CommentDto>> getCommentsByReviewId(@Valid CommentSearchRequest request) {
    CursorPageResponse<CommentDto> response = commentService.getCommentsByReviewId(request);
    return ResponseEntity.ok(response);
  }
}
