package com.deokhugam.deokhugam_server.domain.review.entity;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "review",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_book_user",
            columnNames = {"book_id", "user_id"}
        )
    }
)
public class Review extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(nullable = false)
  private int rating;

  @Builder.Default
  @Column(nullable = false)
  private int likeCount = 0;

  @Builder.Default
  @Column(nullable = false)
  private int commentCount = 0;

  public Review(Book book, User user, String content, int rating) {
    this.book = book;
    this.user = user;
    this.content = content;
    this.rating = rating;
  }

  public void update(String content, int rating) {
    this.content = content;
    this.rating = rating;
  }

  // 수치 정합성을 위한 메서드 추가
  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }

  public void increaseCommentCount() {
    this.commentCount++;
  }

  public void decreaseCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }
}