package com.deokhugam.deokhugam_server.domain.user.entity;

import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
  @Id
  private UUID id;

  @NotBlank(message = "이메일은 필수 입력 값입니다.")
  @Email(message = "이메일 형식이 올바르지 않습니다.")
  @Column(nullable = false, unique = true)
  private String email;

  @NotBlank(message = "닉네임은 필수 입력 값입니다.")
  @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
  @Column(nullable = false, unique = true)
  private String nickname;

  @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
      message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자로 입력해주세요"
  )
  @Column(nullable = false)
  private String password;

  public void updateNickname(String newNickname) {
    this.nickname = newNickname;
  }
}
