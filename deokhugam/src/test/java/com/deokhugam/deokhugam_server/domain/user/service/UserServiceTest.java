package com.deokhugam.deokhugam_server.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.mapper.UserMapper;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServiceImpl userService;

  private User user;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.builder()
        .id(userId)
        .email("test@example.com")
        .nickname("tester")
        .password("encodedPassword")
        .build();
  }

  @Test
  @DisplayName("회원가입 성공")
  void register_success() {
    UserRegisterRequest request = new UserRegisterRequest("test@example.com", "tester", "password123");
    given(userRepository.existsByEmail(anyString())).willReturn(false);
    given(userRepository.existsByNickname(anyString())).willReturn(false);
    given(userMapper.toDto(any(User.class))).willReturn(new UserDto(userId, "test@example.com", "tester", LocalDateTime.now()));
    UserDto result = userService.register(request);

    assertThat(result.email()).isEqualTo(request.email());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 중복")
  void register_fail_duplicateEmail() {
    UserRegisterRequest request = new UserRegisterRequest("test@example.com", "tester", "password123");

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(DeokhugamException.class)
        .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());
  }



}