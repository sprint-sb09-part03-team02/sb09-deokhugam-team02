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
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 1. 무분별한 객체 생성을 막습니다 (C++의 protected 생성자)
@Table(
    name = "review",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_book_user",
            columnNames = {"book_id", "user_id"} // 2. 도서별 1인 1리뷰 제약 조건
        )
    }
)
public class Review extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID) // 3. 명세서에 따른 UUID 자동 생성
  private UUID id;

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(nullable = false)
  private int rating;

  @Column(nullable = false)
  private int likeCount = 0;

  @Column(nullable = false)
  private int commentCount = 0;

  @Builder
  public Review(UUID bookId, UUID userId, String content, int rating) {
    this.bookId = bookId;
    this.userId = userId;
    this.content = content;
    this.rating = rating;
  }

  public void update(String content, int rating) {
    this.content = content;
    this.rating = rating;
  }

  // 좋아요/댓글 수 증감 로직 좋아요/댓글 기능 구현 시 추가
}