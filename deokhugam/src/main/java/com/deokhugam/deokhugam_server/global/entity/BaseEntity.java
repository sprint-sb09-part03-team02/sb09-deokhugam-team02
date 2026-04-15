package com.deokhugam.deokhugam_server.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private boolean isDeleted = false;

  private LocalDateTime deletedAt; // 삭체 시점 기록 필드 추가

  public void delete() {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now(); // 삭제하는 순간의 시간 기록
  }

  public void undoDelete() {
    this.isDeleted = false;
    this.deletedAt = null; // 복구하면 삭제 시간 초기화
  }
}