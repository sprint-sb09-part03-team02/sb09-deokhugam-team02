package com.deokhugam.deokhugam_server.domain.user.mapper;

import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserDto toDto(User user);

  static PowerUserDto toPowerUserDto(PowerUser entity) {
    User user = entity.getUser();

    return new PowerUserDto(
        user.getId(),
        user.getNickname(),
        entity.getPeriodType(),
        entity.getCreatedAt(),
        entity.getRankOrder(),
        entity.getScore(),
        entity.getReviewScoreSum(),
        entity.getLikeCount(),
        entity.getCommentCount()
    );
  }
}
