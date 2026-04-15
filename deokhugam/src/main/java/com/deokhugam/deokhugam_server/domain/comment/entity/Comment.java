package com.deokhugam.deokhugam_server.domain.comment.entity;

import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID reviewId;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private boolean isDeleted = false;

  @Builder
  public Comment(UUID reviewId, UUID userId, String content) {
    this.reviewId = reviewId;
    this.userId = userId;
    this.content = content;
  }

  public void updateContent(String content) {
    this.content = content;
  }

  public void delete() {
    this.isDeleted = true;
  }
}