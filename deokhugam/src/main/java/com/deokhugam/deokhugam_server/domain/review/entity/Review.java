package com.deokhugam.deokhugam_server.domain.review.entity;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "review",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_book_user_is_deleted", // 수정: 논리 삭제 충돌 해결을 위해 is_deleted 추가
            columnNames = {"book_id", "user_id", "is_deleted"}
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

  // --- 추가: 물리 삭제 연쇄 작용을 위한 설정 ---
  @Builder.Default
  @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<ReviewLike> likes = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();
  // ----------------------------------------------------

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
