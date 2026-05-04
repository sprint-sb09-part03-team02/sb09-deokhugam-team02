package com.deokhugam.deokhugam_server.domain.user.service;

import static com.deokhugam.deokhugam_server.global.exception.ErrorCode.*;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.repository.PowerUserRepository;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.mapper.UserMapper;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
      .filter(u -> !u.isDeleted())
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
    String cursor, LocalDateTime after, int limit) {
    Integer cursorInt = parseCursor(cursor);
    LocalDate latestDate = powerUserRepository.findMaxCalculatedDateByPeriodType(period)
      .orElse(LocalDate.now());
    List<PowerUser> results = powerUserRepository.findPowerUsersDynamic(period, cursorInt, after, direction, limit, latestDate);
    boolean hasNext = results.size() > limit;
    List<PowerUser> pagedResults = hasNext ? results.subList(0, limit) : results;

    List<PowerUserDto> content = pagedResults.stream()
      .map(userMapper::toPowerUserDto)
      .toList();
    String nextCursor = null;
    LocalDateTime nextAfter = null;
    if (!content.isEmpty()) {
      PowerUser lastItem = pagedResults.get(pagedResults.size() - 1);
      nextCursor = String.valueOf(lastItem.getRankOrder());
      nextAfter = lastItem.getCreatedAt();
    }

    long totalElements = powerUserRepository.countByPeriodType(period);

    return new CursorPageResponse<>(
      content,
      nextCursor,
      nextAfter,
      content.size(),
      totalElements,
      hasNext
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
    log.info("User soft delete completed. requestUserId={}, targetUserId={}, deletedAt={}",
        requestUserId, targetUserId, user.getDeletedAt());
  }

  @Override
  @Transactional
  public void deleteHard(UUID requestUserId, UUID targetUserId) {
    validateOwner(requestUserId, targetUserId);

    if (!userRepository.existsById(targetUserId)) {
      throw new DeokhugamException(USER_NOT_FOUND);
    }

    userRepository.deleteById(targetUserId);
    log.info("User hard delete completed. requestUserId={}, targetUserId={}",
        requestUserId, targetUserId);
  }
  private void validateOwner(UUID requestUserId, UUID targetUserId) {
    if (!requestUserId.equals(targetUserId)) {
      throw new DeokhugamException(HANDLE_ACCESS_DENIED);
    }
  }
  private Integer parseCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    try {
      return Integer.parseInt(cursor);
    } catch (NumberFormatException e) {
      throw new DeokhugamException(INVALID_INPUT_VALUE);
    }
  }
}
