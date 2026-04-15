package com.deokhugam.deokhugam_server.domain.comment.repository;

import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {
}