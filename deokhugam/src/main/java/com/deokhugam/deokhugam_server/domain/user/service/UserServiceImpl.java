package com.deokhugam.deokhugam_server.domain.user.service;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.mapper.UserMapper;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import java.util.UUID;
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
    User user = userRepository.findByEmail(request.email())
        .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
        .orElseThrow(() -> new RuntimeException("로그인 정보가 일치하지 않습니다."));
    return userMapper.toDto(user);
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
  public void softDelete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 입니다."));
    user.delete();

  }
}
