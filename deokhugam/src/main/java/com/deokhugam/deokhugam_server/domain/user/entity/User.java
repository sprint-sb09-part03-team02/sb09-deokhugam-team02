package com.deokhugam.deokhugam_server.domain.user.entity;

import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {
  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String nickname;

  @Column(nullable = false)
  private String password;

  public void updateNickname(String newNickname) {
    this.nickname = newNickname;
  }
}
