package com.deokhugam.deokhugam_server.domain.comment.entity;

import com.deokhugam.deokhugam_server.domain.review.entity.Review; // 추가된 임포트
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
public class Comment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  // 수정: UUID reviewId 대신 Review 객체를 직접 참조 (물리 삭제 연쇄 반응을 위함)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Builder
  public Comment(Review review, UUID userId, String content) {
    this.review = review;
    this.userId = userId;
    this.content = content;
  }

  public void updateContent(String content) {
    this.content = content;
  }
}
