package com.deokhugam.deokhugam_server.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.mapper.UserMapper;
import com.deokhugam.deokhugam_server.domain.user.repository.PowerUserRepository;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.test.util.ReflectionTestUtils;

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

  @Mock
  private PowerUserRepository powerUserRepository;

  private User user;
  private UUID userId;
  private PowerUser powerUser;

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_NICKNAME = "tester";
  private static final String RAW_PASSWORD = "password123";
  private static final String ENCODED_PASSWORD = "encodedPassword";

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.builder()
        .id(userId)
        .email(TEST_EMAIL)
        .nickname(TEST_NICKNAME)
        .password(ENCODED_PASSWORD)
        .build();
  }

  @Test
  @DisplayName("회원가입 성공")
  void register_success() {
    UserRegisterRequest request = new UserRegisterRequest(TEST_EMAIL, TEST_NICKNAME, RAW_PASSWORD);
    given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
    given(userRepository.existsByNickname(TEST_NICKNAME)).willReturn(false);
    given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);

    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(any(User.class))).willReturn(new UserDto(userId, TEST_EMAIL, TEST_NICKNAME, LocalDateTime.now()));

    UserDto result = userService.register(request);

    assertThat(result.email()).isEqualTo(TEST_EMAIL);

    verify(userRepository).save(argThat(savedUser ->
        savedUser.getEmail().equals(TEST_EMAIL) &&
            savedUser.getPassword().equals(ENCODED_PASSWORD)
    ));
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 중복")
  void register_fail_duplicateEmail() {
    UserRegisterRequest request = new UserRegisterRequest("test@example.com", "tester", "password123");
    given(userRepository.existsByEmail(request.email())).willReturn(true);

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(DeokhugamException.class)
        .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("로그인 성공")
  void login_Success() {
    // given
    UserLoginRequest request = new UserLoginRequest(TEST_EMAIL, RAW_PASSWORD);

    given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
    given(passwordEncoder.matches(RAW_PASSWORD, user.getPassword())).willReturn(true);
    given(userMapper.toDto(user)).willReturn(
        new UserDto(userId, TEST_EMAIL, TEST_NICKNAME, LocalDateTime.now())
    );

    // when
    UserDto result = userService.login(request);

    // then
    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.email()).isEqualTo(TEST_EMAIL);

    verify(userRepository).findByEmail(TEST_EMAIL);
    verify(passwordEncoder).matches(RAW_PASSWORD, user.getPassword());
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void login_Fail_InvalidPassword() {
    // given
    String wrongPassword = "wrong_password";
    UserLoginRequest request = new UserLoginRequest(TEST_EMAIL, wrongPassword);

    given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
    given(passwordEncoder.matches(wrongPassword, user.getPassword())).willReturn(false);

    // when & then
    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(DeokhugamException.class)
        .hasMessage(ErrorCode.LOGIN_FAILED.getMessage());

    verify(userMapper, never()).toDto(any());
  }

  @Test
  @DisplayName("사용자 정보 수정 - 닉네임 변경 성공")
  void update_Success_NewNickname() {
    // given
    String newNickname = "newNickname";
    UserUpdateRequest request = new UserUpdateRequest(newNickname);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByNickname(newNickname)).willReturn(false);

    // when
    userService.update(userId, request);

    // then
    assertThat(user.getNickname()).isEqualTo(newNickname);

    verify(userRepository).findById(userId);
    verify(userRepository).existsByNickname(newNickname);
  }

  @Test
  @DisplayName("사용자 조회 성공")
  void find_Success() {
    // given
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(
        new UserDto(userId, TEST_EMAIL, TEST_NICKNAME, LocalDateTime.now())
    );

    // when
    UserDto result = userService.find(userId);

    // then
    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.email()).isEqualTo(TEST_EMAIL);
    assertThat(result.nickname()).isEqualTo(TEST_NICKNAME);

    verify(userRepository).findById(userId);
    verify(userMapper).toDto(user);
  }

  @Test
  @DisplayName("사용자 조회 실패 - 존재하지 않는 사용자")
  void find_Fail_UserNotFound() {
    // given
    UUID nonExistentId = UUID.randomUUID();
    given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.find(nonExistentId))
        .isInstanceOf(DeokhugamException.class)
        .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    verify(userMapper, never()).toDto(any());
  }

  @Test
  @DisplayName("소프트 삭제 성공")
  void deleteSoft_Success() {
    // given
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when
    userService.deleteSoft(userId);

    // then
    assertThat(user.isDeleted()).isTrue();
    verify(userRepository).findById(userId);
  }

  @Test
  @DisplayName("물리 삭제 성공")
  void deleteHard_Success() {
    // given
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when
    userService.deleteHard(userId);

    // then
    verify(userRepository).findById(userId);
    verify(userRepository).deleteById(userId);
  }

  @Test
  @DisplayName("물리 삭제 실패 - 존재하지 않는 사용자")
  void deleteHard_Fail_UserNotFound() {
    // given
    UUID nonExistentId = UUID.randomUUID();
    given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.deleteHard(nonExistentId))
        .isInstanceOf(DeokhugamException.class)
        .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    verify(userRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("파워 유저 조회 성공")
  void findPowerUsers_Success() {
    // given
    int limit = 1;
    PowerUser user1 = PowerUser.builder().rankOrder(1).build();
    PowerUser user2 = PowerUser.builder().rankOrder(2).build();
    ReflectionTestUtils.setField(user1, "createdAt", LocalDateTime.now());
    ReflectionTestUtils.setField(user2, "createdAt", LocalDateTime.now());

    given(powerUserRepository.findPowerUsersByRequirements(any(), any(), any(), any(), any()))
        .willReturn(List.of(user1, user2));
    given(powerUserRepository.countByPeriodType(any())).willReturn(10L);

    given(userMapper.toPowerUserDto(any())).willReturn(
        new PowerUserDto(userId,"tester", Period.MONTHLY, LocalDateTime.now(), 1, 100.0,
            500.0,10, 5 )
    );

    // when
    CursorPageResponse<PowerUserDto> result = userService.findPowerUsers(Period.MONTHLY, "DESC", null, null, limit);

    // then
    assertThat(result.hasNext()).isTrue();
    assertThat(result.content()).asList().hasSize(1);
    assertThat(result.nextCursor()).isEqualTo("1");
  }

  @Test
  @DisplayName("[RED] 파워 유저 조회 실패 - 커서가 숫자가 아닌 경우")
  void findPowerUsers_Fail_InvalidCursor() {
    // when & then
    assertThatThrownBy(() ->
        userService.findPowerUsers(Period.MONTHLY, "DESC", "abc", null, 10)
    ).isInstanceOf(NumberFormatException.class);
  }
}

