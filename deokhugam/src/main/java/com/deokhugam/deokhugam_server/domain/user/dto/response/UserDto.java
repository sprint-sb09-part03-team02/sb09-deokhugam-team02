package com.deokhugam.deokhugam_server.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(UUID id, String email, String nickname, LocalDateTime createdAt) {

}
