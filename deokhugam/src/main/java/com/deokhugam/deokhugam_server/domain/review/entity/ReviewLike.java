package com.deokhugam.deokhugam_server.domain.review.entity;

import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review_like")
public class ReviewLike extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Builder
  public ReviewLike(UUID reviewId, UUID userId) {
    this.reviewId = reviewId;
    this.userId = userId;
  }

  //중복방지
}