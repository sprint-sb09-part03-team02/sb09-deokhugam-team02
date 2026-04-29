package com.deokhugam.deokhugam_server.domain.notification.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(regexp = "ASC|DESC", message = "정렬 방향은 ASC 또는 DESC만 가능합니다.")
    private String direction = "DESC";

    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
    private Integer limit = 20;
}
