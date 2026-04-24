package com.deokhugam.deokhugam_server.domain.user.controller;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import com.deokhugam.deokhugam_server.global.type.Period;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.service.UserService;
import com.deokhugam.deokhugam_server.global.util.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저 관리", description = "유저 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final JwtProvider jwtProvider;

  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  @PostMapping
  public ResponseEntity<UserDto> register(
    @Valid @RequestBody UserRegisterRequest request) {
    UserDto userDto = userService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
  @PostMapping("/login")
  public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest request) {
    UserDto userDto = userService.login(request);
    String token = jwtProvider.generateToken(userDto.id());
    return ResponseEntity.status(HttpStatus.OK)
        .header("Deokhugam-Request-User-ID", userDto.id().toString())
        .header("Authorization", "Bearer " + token)
        .body(userDto);
  }

  @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 상세 정보를 조회합니다.")
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> findById(
    @RequestHeader(value = "Deokhugam-Request-User-ID", required = false) UUID requestUserId,
    @PathVariable UUID userId) {
    UserDto user = userService.find(requestUserId,userId);
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @Operation(summary = "사용자 논리 삭제", description = "사용자를 논리적으로 삭제합니다.")
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(
    @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
    @PathVariable UUID userId) {
    userService.deleteSoft(requestUserId, userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Operation(summary = "사용자 정보 수정", description = "사용자의 닉네임을 수정합니다.")
  @PatchMapping("/{userId}")
  public ResponseEntity<UserDto> update(
    @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
    @PathVariable UUID userId, @Valid @RequestBody UserUpdateRequest request) {
    UserDto user =  userService.update(requestUserId, userId, request);
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @Operation(summary = "파워 유저 목록 조회", description = "기간별 파워 유저 목록을 조회합니다.")
  @GetMapping("/power")
  public ResponseEntity<CursorPageResponse<PowerUserDto>> searchPowerUser(
      @RequestParam(defaultValue = "DAILY") Period period,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    CursorPageResponse<PowerUserDto> response = userService.findPowerUsers(period, direction,
        cursor, after, limit);

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "사용자 물리 삭제", description = "사용자를 물리적으로 삭제합니다.")
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> deleteHard(
    @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
    @PathVariable UUID userId) {
    userService.deleteHard(requestUserId, userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
