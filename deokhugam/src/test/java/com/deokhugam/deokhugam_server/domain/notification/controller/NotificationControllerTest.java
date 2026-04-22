package com.deokhugam.deokhugam_server.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.deokhugam_server.domain.notification.dto.response.NotificationDto;
import com.deokhugam.deokhugam_server.domain.notification.entity.NotificationType;
import com.deokhugam.deokhugam_server.domain.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    private static final String BASE_URL = "/api/notifications";
    private static final String USER_HEADER = "Deokhugam-Request-User-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    private final UUID notificationId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("성공: 알림 읽음 상태 업데이트는 요청 바디를 받아 처리한다")
    void updateNotification_Success() throws Exception {
        NotificationUpdateRequest request = new NotificationUpdateRequest(true);
        NotificationDto response = new NotificationDto(
            notificationId,
            UUID.randomUUID(),
            userId,
            NotificationType.REVIEW_COMMENTED,
            "알림 내용",
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        given(notificationService.readNotification(eq(notificationId), eq(userId), any(NotificationUpdateRequest.class)))
            .willReturn(response);

        mockMvc.perform(patch(BASE_URL + "/{notificationId}", notificationId)
                .header(USER_HEADER, userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패: 알림 읽음 상태가 없으면 400을 반환한다")
    void updateNotification_Fail_WhenConfirmedMissing() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{notificationId}", notificationId)
                .header(USER_HEADER, userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }
}
