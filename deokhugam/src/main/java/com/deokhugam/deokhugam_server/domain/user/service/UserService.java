package com.deokhugam.deokhugam_server.domain.user.service;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import java.util.UUID;

public interface UserService {
  UserDto register(UserRegisterRequest request);
  UserDto login(UserLoginRequest request);
  void update(UUID userId, UserUpdateRequest request);
  void softDelete(UUID userId);

}
