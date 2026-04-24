package com.deokhugam.deokhugam_server.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.mapper.UserMapper;
import com.deokhugam.deokhugam_server.domain.user.repository.PowerUserRepository;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepositoryCustom;
import com.deokhugam.deokhugam_server.domain.user.service.PowerUserService;
import com.deokhugam.deokhugam_server.domain.user.service.UserService;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.global.util.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private PowerUserService powerUserService;

  @MockitoBean
  private JwtProvider jwtProvider;

  private UserDto commonResponse;

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_PASSWORD = "Password123!";
  private static final String TEST_NICKNAME = "nickname";

  @BeforeEach
  void setUp() {
    commonResponse = new UserDto(UUID.randomUUID(), "test@example.com", "nickname", LocalDateTime.now());
  }

  @Test
  @DisplayName("회원가입 성공")
  void register_Success() throws Exception {
    // given
    UserRegisterRequest request = new UserRegisterRequest("test@example.com", "nickname", "Password123!");
    given(userService.register(any())).willReturn(commonResponse);
    given(userService.register(any(UserRegisterRequest.class))).willReturn(commonResponse);

    // when & then
    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.email").value(commonResponse.email()))
      .andExpect(jsonPath("$.nickname").value(commonResponse.nickname()));
  }

  @Test
  @DisplayName("로그인 성공")
  void login_Success() throws Exception {
    // given
    UserLoginRequest request = new UserLoginRequest(TEST_EMAIL, TEST_PASSWORD);
    given(userService.login(any())).willReturn(commonResponse);

    // when & then
    mockMvc.perform(post("/api/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(commonResponse.id().toString()))
      .andExpect(jsonPath("$.email").value(TEST_EMAIL))
      .andExpect(jsonPath("$.nickname").value(TEST_NICKNAME))
      .andExpect(jsonPath("$.createdAt").exists());
  }


  @Test
  @DisplayName("사용자 정보 조회 성공")
  void findById_Success() throws Exception {
    // given
    UUID targetUserId = commonResponse.id();
    given(userService.find(any(), eq(targetUserId))).willReturn(commonResponse);

    // when
    var resultActions = mockMvc.perform(get("/api/users/{userId}", targetUserId)
      .header("Deokhugam-Request-User-ID", targetUserId)
      .accept(MediaType.APPLICATION_JSON));

    // Then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(targetUserId.toString()))
      .andExpect(jsonPath("$.email").value(TEST_EMAIL))
      .andExpect(jsonPath("$.nickname").value(TEST_NICKNAME))
      .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  @DisplayName("사용자 논리 삭제 성공")
  void delete_Success() throws Exception {
    // given
    UUID targetUserId = commonResponse.id();
    UUID requestUserId = commonResponse.id();
    willDoNothing().given(userService).deleteSoft(eq(requestUserId), eq(targetUserId));

    // when
    var resultActions = mockMvc.perform(delete("/api/users/{userId}", targetUserId)
      .header("Deokhugam-Request-User-ID", requestUserId));

    // Then
    resultActions.andExpect(status().isNoContent());
    verify(userService, times(1)).deleteSoft(eq(requestUserId), eq(targetUserId));
  }

  @Test
  @DisplayName("사용자 정보 수정 성공")
  void update_Success() throws Exception {
    // given
    UUID targetUserId = commonResponse.id();
    String newNickname = "newNickname";

    UserUpdateRequest updateRequest = new UserUpdateRequest(newNickname);
    UserDto updatedResponse = new UserDto(targetUserId, TEST_EMAIL, newNickname, LocalDateTime.now());

    given(userService.update(eq(targetUserId), eq(targetUserId), any(UserUpdateRequest.class)))
      .willReturn(updatedResponse);

    // when
    var resultActions = mockMvc.perform(patch("/api/users/{userId}", targetUserId)
      .header("Deokhugam-Request-User-ID", targetUserId)
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(updateRequest)));

    // Then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(targetUserId.toString()))
      .andExpect(jsonPath("$.nickname").value(newNickname))
      .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    verify(userService).update(eq(targetUserId), eq(targetUserId), any(UserUpdateRequest.class));
  }

  @Test
  @DisplayName("파워 유저 목록 조회 성공")
  void searchPowerUser_Success() throws Exception {
    // given
    PowerUserDto powerUser = new PowerUserDto(
      commonResponse.id(), TEST_NICKNAME, Period.WEEKLY, LocalDateTime.now(),    // createdAt
      1, 100.0, 80.0, 10, 5  );
    CursorPageResponse<PowerUserDto> pageResponse = new CursorPageResponse<>(
      List.of(powerUser),"next-cursor-id", LocalDateTime.now().plusDays(1), // nextAfter
      1,100L, true);
    given(userService.findPowerUsers(any(Period.class), anyString(), any(), any(), anyInt()))
      .willReturn(pageResponse);

    // when
    var resultActions = mockMvc.perform(get("/api/users/power")
      .param("period", "WEEKLY")
      .param("direction", "DESC")
      .param("limit", "10")
      .accept(MediaType.APPLICATION_JSON));

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].userId").value(commonResponse.id().toString()))
      .andExpect(jsonPath("$.content[0].nickname").value(TEST_NICKNAME))
      .andExpect(jsonPath("$.content[0].rank").value(1))
      .andExpect(jsonPath("$.nextCursor").value("next-cursor-id"))
      .andExpect(jsonPath("$.hasNext").value(true))
      .andExpect(jsonPath("$.size").value(1));
    verify(userService, times(1)).findPowerUsers(eq(Period.WEEKLY), eq("DESC"), any(), any(), eq(10));
  }

  @Test
  @DisplayName("사용자 물리 삭제 성공")
  void deleteHard_Success() throws Exception {
    // given
    UUID targetUserId = commonResponse.id();
    UUID requestUserId = commonResponse.id();

    willDoNothing().given(userService).deleteHard(eq(requestUserId), eq(targetUserId));

    // when
    var resultActions = mockMvc.perform(delete("/api/users/{userId}/hard", targetUserId)
      .header("Deokhugam-Request-User-ID", requestUserId));

    // then
    resultActions.andExpect(status().isNoContent());
    verify(userService, times(1)).deleteHard(eq(requestUserId), eq(targetUserId));
  }
}
