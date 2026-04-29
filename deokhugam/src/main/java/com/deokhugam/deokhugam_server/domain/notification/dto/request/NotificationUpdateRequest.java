package com.deokhugam.deokhugam_server.domain.notification.dto.request;

import jakarta.validation.constraints.NotNull;

public record NotificationUpdateRequest(
    @NotNull(message = "알림 읽음 상태는 필수입니다.")
    Boolean confirmed
) {}
