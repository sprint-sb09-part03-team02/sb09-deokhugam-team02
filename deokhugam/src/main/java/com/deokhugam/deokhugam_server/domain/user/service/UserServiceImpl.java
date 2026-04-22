package com.deokhugam.deokhugam_server.domain.user.service;

import static com.deokhugam.deokhugam_server.global.exception.ErrorCode.*;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.repository.PowerUserRepository;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.mapper.UserMapper;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final PowerUserRepository powerUserRepository;

  @Override
  @Transactional
  public UserDto register(UserRegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new DeokhugamException(DUPLICATE_EMAIL);
    }
    if (userRepository.existsByNickname(request.nickname())) {
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
  public CursorPageResponse<PowerUserDto> findPowerUsers(Period period, String direction,
      String cursor, String after, int limit) {
    Integer cursorRank = null;
    if (cursor != null && !cursor.isBlank()) {
      try {
        cursorRank = Integer.parseInt(cursor);
      } catch (NumberFormatException e) {
        throw new DeokhugamException(ErrorCode.INVALID_INPUT_VALUE);
      }
    }
    LocalDateTime afterLdt = parseLocalDateTime(after);

    List<PowerUser> powerUsers = powerUserRepository.findPowerUsersByRequirements(
        period, direction.toUpperCase(), cursorRank, afterLdt,
        Limit.of(limit + 1)
    );

    long totalElements = powerUserRepository.countByPeriodType(period);

    boolean hasNext = powerUsers.size() > limit;
    List<PowerUser> content = hasNext ? powerUsers.subList(0, limit) : powerUsers;

    String nextCursor =
        content.isEmpty() ? null : String.valueOf(content.get(content.size() - 1).getRankOrder());
    LocalDateTime nextAfter =
        content.isEmpty() ? null : content.get(content.size() - 1).getCreatedAt();
    return new CursorPageResponse<>(
        content.stream().map(userMapper::toPowerUserDto).toList(),
        nextCursor, nextAfter, content.size(), totalElements, hasNext
    );
  }

  @Override
  @Transactional
  public UserDto update(UUID userId, UserUpdateRequest request) {
    User user = userRepository.findById(userId).orElseThrow(() ->
        new DeokhugamException(USER_NOT_FOUND));
    if (!user.getNickname().equals(request.nickname())) {
      if (userRepository.existsByNickname(request.nickname())) {
        throw new DeokhugamException(DUPLICATE_NICKNAME);
      }
      user.updateNickname(request.nickname());
    }
    return userMapper.toDto(user);
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
  private LocalDateTime parseLocalDateTime(String after) {
    if (after == null || after.isBlank())
      return null;
    try {
      ZoneId kstZone = ZoneId.of("Asia/Seoul");
      if (after.endsWith("Z")) {
        return LocalDateTime.ofInstant(Instant.parse(after), kstZone);
      }
      return LocalDateTime.parse(after);
    } catch (Exception e) {
      return LocalDate.parse(after.substring(0, 10)).atStartOfDay();
    }
  }

}
