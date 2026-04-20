package com.deokhugam.deokhugam_server.domain.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationSearchRequest {

    @NotNull(message = "사용자 ID는 필수 조회 조건입니다.")
    private UUID userId;

    private String cursor;
    private LocalDateTime after;

    private String direction = "DESC";
    private Integer limit = 20;
}
