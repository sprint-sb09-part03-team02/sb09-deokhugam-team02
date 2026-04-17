package com.deokhugam.deokhugam_server.domain.user.mapper;

import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserDto toDto(User user);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "nickname", source = "user.nickname")
  @Mapping(target = "rank", source = "rankOrder")
  @Mapping(target = "period", source = "periodType")
  PowerUserDto toPowerUserDto(PowerUser entity);
}
