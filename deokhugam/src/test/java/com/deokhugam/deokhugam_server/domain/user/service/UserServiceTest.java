package com.deokhugam.deokhugam_server.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.mapper.UserMapper;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import java.time.LocalDateTime;
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

  @BeforeEach
  void setUp() {
  }

  @Test
  @DisplayName("회원가입 성공")
  void register_success() {
    UserRegisterRequest request = new UserRegisterRequest("test@test.com", "덕후", "pwd123!");
    User user = User.builder().email(request.email()).build();
    UserDto expectedDto = new UserDto(UUID.randomUUID(), "test@test.com", "덕후", LocalDateTime.now());

    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(userRepository.existsByNickname(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("hashed_pwd");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toDto(any(User.class))).thenReturn(expectedDto);

    UserDto result = userService.register(request);

    assertNotNull(result);
    assertEquals(expectedDto.email(), result.email());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 중복")
  void register_fail_duplicateEmail() {
    UserRegisterRequest request = new UserRegisterRequest("duplicate@test.com", "지은", "pwd123!");
    when(userRepository.existsByEmail(request.email())).thenReturn(true); // 중복되었다고 가정!

    DeokhugamException exception = assertThrows(DeokhugamException.class, () -> {
      userService.register(request);
    });

    assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
  }

  @Test
  void login() {
  }

  @Test
  void find() {
  }

  @Test
  void findPowerUsers() {
  }

  @Test
  void update() {
  }

  @Test
  void deleteSoft() {
  }

  @Test
  void deleteHard() {
  }
}