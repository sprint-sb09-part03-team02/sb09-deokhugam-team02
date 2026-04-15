package com.deokhugam.deokhugam_server.domain.user.service;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.Period;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.mapper.UserMapper;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
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

  @Override
  @Transactional
  public UserDto register(UserRegisterRequest request) {
    if(userRepository.existsByEmail(request.email())) {
      throw new RuntimeException("이미 존재하는 이메일 입니다.");
    }
    if(userRepository.existsByNickname(request.nickname())) {
      throw new RuntimeException("이미 존재하는 닉네임 입니다.");
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
        .orElseThrow(() -> new RuntimeException("로그인 정보가 일치하지 않습니다."));
  }

  @Override
  public UserDto find(UUID userId) {
    return userRepository.findById(userId)
        .map(userMapper::toDto)
        .orElseThrow(() -> new RuntimeException("일치하는 사용자 정보가 없습니다."));
  }

  @Override
  public List<UserDto> findPowerUsers(Period period, String direction, String cursor, String after,
      int limit) {
    LocalDateTime startTime = calculateStartTime(period);
    List<User> powerUsers = userRepository.findPowerUsersWithPaging(
        startTime, direction, cursor, after, limit
    );
    return powerUsers.stream()
        .map(userMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void update(UUID userId, UserUpdateRequest request) {
    User user = userRepository.findById(userId).orElseThrow(() ->
        new RuntimeException("존재하지 않는 사용자 입니다."));
    if (!user.getNickname().equals(request.nickname())) {
      if (userRepository.existsByNickname(request.nickname())) {
        throw new RuntimeException("이미 존재하는 닉네임 입니다.");
      }
      user.updateNickname(request.nickname());
    }
  }

  @Override
  @Transactional
  public void deleteSoft(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 입니다."));
    user.delete();

  }

  @Override
  public void deleteHard(UUID userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 입니다."));
    userRepository.deleteById(userId);
  }

  private LocalDateTime calculateStartTime(Period period) {
    return switch (period) {
      case DAILY -> LocalDateTime.now().minusDays(1);
      case WEEKLY -> LocalDateTime.now().minusWeeks(1);
      case MONTHLY -> LocalDateTime.now().minusMonths(1);
      case ALL_TIME -> LocalDateTime.of(2020, 1, 1, 0, 0); // 아주 오래전 시간
    };
  }
}
