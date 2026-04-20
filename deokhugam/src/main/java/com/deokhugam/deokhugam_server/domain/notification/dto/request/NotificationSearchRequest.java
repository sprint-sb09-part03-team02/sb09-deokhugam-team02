package com.deokhugam.deokhugam_server.domain.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class NotificationSearchRequest {

    @NotNull(message = "사용자 ID는 필수 조회 조건입니다.")
    private UUID userId;

    private String cursor;
    private LocalDateTime after;

    private String direction = "DESC";
    private Integer limit = 20;

    // 컨트롤러에서 헤더 기반 userId를 주입할 때만 사용
    public void assignUserId(UUID userId) {
        this.userId = userId;
    }
}
