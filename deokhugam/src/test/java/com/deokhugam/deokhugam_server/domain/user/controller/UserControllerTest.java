package com.deokhugam.deokhugam_server.domain.user.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.service.PowerUserService;
import com.deokhugam.deokhugam_server.domain.user.service.UserService;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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
  void delete() {
  }

  @Test
  void update() {
  }

  @Test
  void searchPowerUser() {
  }

  @Test
  void deleteHard() {
  }
}
