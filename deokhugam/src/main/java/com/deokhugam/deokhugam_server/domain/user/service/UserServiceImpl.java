package com.deokhugam.deokhugam_server.domain.user.service;

import static com.deokhugam.deokhugam_server.global.exception.ErrorCode.*;
import static com.deokhugam.deokhugam_server.global.util.DateTimeUtils.parseLocalDateTime;

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
import java.time.LocalDateTime;
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
  public UserDto find(UUID requestUserId, UUID targetUserId) {
    User user = userRepository.findById(targetUserId)
      .orElseThrow(() -> new DeokhugamException(USER_NOT_FOUND));

    return userMapper.toDto(user);
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
  public UserDto update(UUID requestUserId, UUID targetUserId, UserUpdateRequest request) {
    validateOwner(requestUserId, targetUserId);
    User user = userRepository.findById(targetUserId).orElseThrow(() ->
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
  public void deleteSoft(UUID requestUserId, UUID targetUserId) {
    validateOwner(requestUserId, targetUserId);

    User user = userRepository.findById(targetUserId)
      .orElseThrow(() -> new DeokhugamException(USER_NOT_FOUND));

    user.delete();

  }

  @Override
  @Transactional
  public void deleteHard(UUID requestUserId, UUID targetUserId) {
    validateOwner(requestUserId, targetUserId);

    if (!userRepository.existsById(targetUserId)) {
      throw new DeokhugamException(USER_NOT_FOUND);
    }

    userRepository.deleteById(targetUserId);
  }
  private void validateOwner(UUID requestUserId, UUID targetUserId) {
    if (!requestUserId.equals(targetUserId)) {
      throw new DeokhugamException(HANDLE_ACCESS_DENIED);
    }
  }

}
