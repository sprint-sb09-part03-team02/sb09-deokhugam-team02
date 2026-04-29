package com.deokhugam.deokhugam_server.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deokhugam.deokhugam_server.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.deokhugam_server.domain.notification.dto.response.NotificationDto;
import com.deokhugam.deokhugam_server.domain.notification.service.NotificationService;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
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
    @DisplayName("성공: 알림 목록 조회는 query userId를 받아 처리한다")
    void getNotifications_Success() throws Exception {
        UUID reviewId = UUID.randomUUID();
        NotificationDto notification = new NotificationDto(
            notificationId,
            userId,
            reviewId,
            "리뷰 내용",
            "알림 내용",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        CursorPageResponse<NotificationDto> response = new CursorPageResponse<>(
            List.of(notification),
            null,
            null,
            1,
            1L,
            false
        );
        given(notificationService.getNotifications(any())).willReturn(response);

        mockMvc.perform(get(BASE_URL)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(notificationId.toString()))
            .andExpect(jsonPath("$.content[0].userId").value(userId.toString()))
            .andExpect(jsonPath("$.content[0].reviewId").value(reviewId.toString()))
            .andExpect(jsonPath("$.content[0].reviewContent").value("리뷰 내용"))
            .andExpect(jsonPath("$.content[0].message").value("알림 내용"))
            .andExpect(jsonPath("$.content[0].confirmed").value(false));
    }

    @Test
    @DisplayName("실패: 알림 목록 조회 시 userId 쿼리가 없으면 400을 반환한다")
    void getNotifications_Fail_WhenUserIdMissing() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 알림 목록 조회 시 direction 값이 잘못되면 400을 반환한다")
    void getNotifications_Fail_WhenDirectionInvalid() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("userId", userId.toString())
                .param("direction", "INVALID"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공: 알림 읽음 상태 업데이트는 요청 바디를 받아 처리한다")
    void updateNotification_Success() throws Exception {
        NotificationUpdateRequest request = new NotificationUpdateRequest(true);
        UUID reviewId = UUID.randomUUID();
        NotificationDto response = new NotificationDto(
            notificationId,
            userId,
            reviewId,
            "리뷰 내용",
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
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(notificationId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
            .andExpect(jsonPath("$.reviewContent").value("리뷰 내용"))
            .andExpect(jsonPath("$.message").value("알림 내용"))
            .andExpect(jsonPath("$.confirmed").value(true));
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

    @Test
    @DisplayName("성공: 모든 알림 읽음 처리는 헤더 사용자 ID로 처리한다")
    void readAllNotifications_Success() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/read-all")
                .header(USER_HEADER, userId.toString()))
            .andExpect(status().isNoContent());
    }
}
