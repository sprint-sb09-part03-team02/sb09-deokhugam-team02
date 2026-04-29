package com.deokhugam.deokhugam_server.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserLoginRequest(
  @NotBlank(message = "이메일은 필수 입력 값입니다.")
  @Email(message = "이메일 형식이 올바르지 않습니다.")
  String email,

  @NotBlank
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
    message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자로 입력해주세요")
  String password) {

}
