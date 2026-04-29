package com.deokhugam.deokhugam_server.domain.book.entity;

import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "author", nullable = false, length = 255)
  private String author;

  @Column(name = "isbn", nullable = false, unique = true, length = 20)
  private String isbn;

  @Column(name = "publisher", length = 255)
  private String publisher;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "thumbnail_url", length = 1000)
  private String thumbnailUrl;

  @Column(name = "published_date", nullable = false)
  private LocalDate publishedDate;

  @Column(name = "review_count", nullable = false)
  private int reviewCount;

  @Column(name = "rating", nullable = false)
  private double rating;

  public Book(
    String title,
    String author,
    String isbn,
    String publisher,
    String description,
    String thumbnailUrl,
    LocalDate publishedDate
  ) {
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.publisher = publisher;
    this.description = description;
    this.thumbnailUrl = thumbnailUrl;
    this.publishedDate = publishedDate;
    this.reviewCount = 0;
    this.rating = 0.0;
  }

  public void update(
    String title,
    String author,
    String publisher,
    String description,
    String thumbnailUrl,
    LocalDate publishedDate
  ) {
    if (title != null) {
      this.title = title;
    }
    if (author != null) {
      this.author = author;
    }
    if (publisher != null) {
      this.publisher = publisher;
    }
    if (description != null) {
      this.description = description;
    }
    if (thumbnailUrl != null) {
      this.thumbnailUrl = thumbnailUrl;
    }
    if (publishedDate != null) {
      this.publishedDate = publishedDate;
    }
  }

  public void updateReviewStatistics(int reviewCount, double rating) {
    this.reviewCount = reviewCount;
    this.rating = rating;
  }
}
