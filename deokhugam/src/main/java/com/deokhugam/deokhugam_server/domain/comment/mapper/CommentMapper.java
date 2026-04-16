package com.deokhugam.deokhugam_server.domain.comment.mapper;

import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  @Mapping(target = "userNickname", source = "userNickname")
  CommentDto toDto(Comment comment, String userNickname);
}