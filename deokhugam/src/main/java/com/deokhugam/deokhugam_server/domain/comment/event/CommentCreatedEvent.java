package com.deokhugam.deokhugam_server.domain.comment.event;

import java.util.UUID;

public record CommentCreatedEvent(
    UUID reviewId,
    UUID commentId,
    UUID commentAuthorId,
    String commentAuthorNickname
) {}
