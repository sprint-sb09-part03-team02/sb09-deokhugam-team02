package com.deokhugam.deokhugam_server.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
    @NotBlank(message = "수정할 댓글 내용을 입력해주세요.")
    String content
) {}