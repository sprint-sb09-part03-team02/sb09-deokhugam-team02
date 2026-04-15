package com.deokhugam.deokhugam_server.domain.comment.dto.request;

import java.util.UUID;

public record CommentCreateRequest(
    UUID reviewId,
    String content
) {}