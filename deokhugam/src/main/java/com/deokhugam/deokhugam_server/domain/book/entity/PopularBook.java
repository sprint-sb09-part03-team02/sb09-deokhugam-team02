package com.deokhugam.deokhugam_server.domain.book.entity;

import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
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
public class PopularBook extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id")
  private Book book;

  @Enumerated(EnumType.STRING)
  private Period periodType;

  private Double score;

  private Integer rankOrder;

  private LocalDate calculatedDate;

  private Long reviewCount;

  private Double rating;

  public void assignRankOrder(int rank) {
    this.rankOrder = rank;
  }
}
