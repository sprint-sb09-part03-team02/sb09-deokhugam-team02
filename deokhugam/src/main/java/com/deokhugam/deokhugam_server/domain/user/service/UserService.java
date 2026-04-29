package com.deokhugam.deokhugam_server.domain.user.service;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UserService {
  UserDto register(UserRegisterRequest request);

  UserDto login(UserLoginRequest request);

  UserDto find( UUID requestUserId, UUID targetUserId);

  CursorPageResponse<PowerUserDto> findPowerUsers(Period period, String direction, String cursor, LocalDateTime after, int limit);

  UserDto update( UUID requestUserId, UUID targetUserId, UserUpdateRequest request);

  void deleteSoft(UUID requestUserId, UUID targetUserId);

  void deleteHard( UUID requestUserId, UUID targetUserId);

}
