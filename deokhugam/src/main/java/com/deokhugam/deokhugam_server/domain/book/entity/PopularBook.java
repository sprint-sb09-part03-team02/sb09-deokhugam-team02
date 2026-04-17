package com.deokhugam.deokhugam_server.domain.book.entity;

import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import com.deokhugam.deokhugam_server.global.type.Period;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "popular_books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularBook extends BaseEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false)
  private Period periodType;

  @Column(name = "score", nullable = false)
  private Double score;

  @Column(name = "rank_order", nullable = false)
  private Integer rankOrder;

  @Column(name = "calculated_date", nullable = false)
  private LocalDate calculatedDate;

  @Column(name = "review_count", nullable = false)
  private Long reviewCount;

  @Column(name = "rating", nullable = false)
  private Double rating;

  public PopularBook(
          Book book,
          Period periodType,
          Double score,
          Integer rankOrder,
          LocalDate calculatedDate,
          Long reviewCount,
          Double rating
  ) {
    this.book = book;
    this.periodType = periodType;
    this.score = score;
    this.rankOrder = rankOrder;
    this.calculatedDate = calculatedDate;
    this.reviewCount = reviewCount;
    this.rating = rating;
  }

  public void assignRankOrder(int rank) {
    this.rankOrder = rank;
  }
}