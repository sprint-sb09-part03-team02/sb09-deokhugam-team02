package com.deokhugam.deokhugam_server.domain.review.entity;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
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

  @Column(nullable = false)
  private int likeCount = 0;

  @Column(nullable = false)
  private int commentCount = 0;

  @Builder
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

  // 좋아요/댓글 수 증감 로직 좋아요/댓글 기능 구현 시 추가
}