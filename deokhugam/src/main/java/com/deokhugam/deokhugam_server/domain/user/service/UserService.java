package com.deokhugam.deokhugam_server.domain.user.service;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import java.util.List;
import java.util.UUID;

public interface UserService {
  UserDto register(UserRegisterRequest request);

  UserDto login(UserLoginRequest request);

  UserDto find(UUID userId);

  List<UserDto> findPowerUsers(Period period, String direction, String cursor, String after, int limit);

  void update(UUID userId, UserUpdateRequest request);

  void deleteSoft(UUID userId);

  void deleteHard(UUID userId);

}
