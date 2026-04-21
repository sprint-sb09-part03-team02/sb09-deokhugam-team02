package com.deokhugam.deokhugam_server.domain.comment.entity;

import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity { // BaseEntity를 상속받음

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID reviewId;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Builder
  public Comment(UUID reviewId, UUID userId, String content) {
    this.reviewId = reviewId;
    this.userId = userId;
    this.content = content;
  }

  public void updateContent(String content) {
    this.content = content;
  }
}