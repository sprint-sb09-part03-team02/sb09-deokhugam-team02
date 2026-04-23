package com.deokhugam.deokhugam_server.domain.user.entity;

import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
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
public class PowerUser extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "UUID")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  private Period periodType;

  @Column(name = "score", nullable = false)
  private Double score;

  @Column(name = "review_score_sum")
  private Double reviewScoreSum;

  @Column(name = "rank_order", nullable = false)
  private Integer rankOrder;

  @Column(name = "calculated_date", nullable = false)
  private LocalDate calculatedDate;

  @Column(name = "like_count")
  private Long likeCount;

  @Column(name = "comment_count")
  private Long commentCount;

  public void assignRankOrder(int rank) {
    this.rankOrder = rank;
  }

}
