package com.deokhugam.deokhugam_server.domain.user.controller;

import com.deokhugam.deokhugam_server.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.deokhugam_server.domain.user.dto.response.Period;
import com.deokhugam.deokhugam_server.domain.user.dto.response.UserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> register(
    @Valid @RequestBody UserRegisterRequest request) {
    UserDto userDto = userService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  @PostMapping("/login")
  public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest request) {
    UserDto userDto = userService.login(request);
    return ResponseEntity.status(HttpStatus.OK)
        .header("Deokhugam-Request-User-ID", userDto.id().toString())
        .body(userDto);
  }
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> findById(@PathVariable UUID userId) {
    UserDto user = userService.find(userId);
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    userService.deleteSoft(userId);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<Void> update(@PathVariable UUID userId, @Valid @RequestBody UserUpdateRequest request) {
    userService.update(userId, request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("/power")
  public ResponseEntity<List<UserDto>> findPowerUser(
      @RequestParam(defaultValue = "DAILY") Period period,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    List<UserDto> powerUsers = userService.findPowerUsers(period, direction, cursor, after, limit);
    return ResponseEntity.ok(powerUsers);
  }

  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> deleteHard(@PathVariable UUID userId) {
    userService.deleteHard(userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
