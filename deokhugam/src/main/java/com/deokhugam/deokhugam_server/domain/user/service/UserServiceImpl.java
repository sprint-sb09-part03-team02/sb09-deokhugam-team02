package com.deokhugam.deokhugam_server.domain.user.service;

import static com.deokhugam.deokhugam_server.global.exception.ErrorCode.*;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.repository.PowerUserRepository;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.mapper.UserMapper;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final PowerUserRepository powerUserRepository;

  @Override
  @Transactional
  public UserDto register(UserRegisterRequest request) {
    if(userRepository.existsByEmail(request.email())) {
      throw new DeokhugamException(DUPLICATE_EMAIL);
    }
    if(userRepository.existsByNickname(request.nickname())) {
      throw new DeokhugamException(DUPLICATE_NICKNAME);
    }
    String encodedPassword = passwordEncoder.encode(request.password());
    User user = User.builder()
        .id(UUID.randomUUID())
        .email(request.email())
        .nickname(request.nickname())
        .password(encodedPassword)
        .build();
    User savedUser = userRepository.save(user);
    return userMapper.toDto(savedUser);
  }

  @Override
  public UserDto login(UserLoginRequest request) {
    return userRepository.findByEmail(request.email())
        .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
        .map(userMapper::toDto)
        .orElseThrow(() -> new DeokhugamException(LOGIN_FAILED));
  }

  @Override
  public UserDto find(UUID userId) {
    return userRepository.findById(userId)
        .map(userMapper::toDto)
        .orElseThrow(() -> new DeokhugamException(USER_NOT_FOUND));
  }

  @Override
  public List<PowerUserDto> findPowerUsers(Period period, String direction, String cursor, String after,
      int limit) {
    Integer cursorRank = (cursor != null && !cursor.isBlank()) ? Integer.parseInt(cursor) : null;
    LocalDateTime afterLdt = null;
    if (after != null && !after.isBlank()) {
      try {
        if (after.endsWith("Z")) {
          afterLdt = LocalDateTime.ofInstant(Instant.parse(after), ZoneOffset.UTC);
        } else {
          afterLdt = LocalDateTime.parse(after);
        }
      } catch (Exception e) {
        afterLdt = LocalDate.parse(after.substring(0, 10)).atStartOfDay();
      }
    }
      List<PowerUser> powerUsers = powerUserRepository.findPowerUsersByRequirements(
          period, direction.toUpperCase(), cursorRank, afterLdt, limit
      );
      return powerUsers.stream()
          .map(userMapper::toPowerUserDto)
          .collect(Collectors.toList());
    }

  @Override
  @Transactional
  public void update(UUID userId, UserUpdateRequest request) {
    User user = userRepository.findById(userId).orElseThrow(() ->
        new DeokhugamException(USER_NOT_FOUND));
    if (!user.getNickname().equals(request.nickname())) {
      if (userRepository.existsByNickname(request.nickname())) {
        throw new DeokhugamException(DUPLICATE_NICKNAME);
      }
      user.updateNickname(request.nickname());
    }
  }

  @Override
  @Transactional
  public void deleteSoft(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new DeokhugamException(USER_NOT_FOUND));
    user.delete();

  }

  @Override
  public void deleteHard(UUID userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> new DeokhugamException(USER_NOT_FOUND));
    userRepository.deleteById(userId);
  }

}
