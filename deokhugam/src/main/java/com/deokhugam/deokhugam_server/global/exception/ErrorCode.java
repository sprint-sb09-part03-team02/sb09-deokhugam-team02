package com.deokhugam.deokhugam_server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // [COMMON] 공통 에러 - 명세서 400, 500 대응
  INVALID_INPUT_VALUE(400, "C001", "잘못된 요청 (입력값 검증 실패)"),
  INTERNAL_SERVER_ERROR(500, "C002", "서버 내부 오류가 발생했습니다."),
  METHOD_NOT_ALLOWED(405, "C003", "허용되지 않은 메소드입니다."),
  HANDLE_ACCESS_DENIED(403, "C004", "접근 권한이 없습니다."),

  /*
   * [USER] 사용자 관리
   * 명세서 /api/users, /api/users/login 대응
   */
  LOGIN_FAILED(401, "U101", "로그인 실패 (이메일 또는 비밀번호 불일치)"),
  DUPLICATE_EMAIL(409, "U102", "이메일 중복"),
  USER_NOT_FOUND(404, "U103", "사용자 정보 없음"),

  /*
   * [BOOK] 도서 관리
   * 명세서 /api/books 대응
   */
  BOOK_NOT_FOUND(404, "B201", "도서 정보 없음"),
  DUPLICATE_ISBN(409, "B202", "ISBN 중복"),

  // [REVIEW] 리뷰 관리
  // 명세서 /api/reviews 대응
  REVIEW_NOT_FOUND(404, "R301", "리뷰 정보 없음"),
  ALREADY_REVIEWED(409, "R302", "이미 작성된 리뷰 존재"),
  NOT_REVIEW_OWNER(403, "R303", "리뷰 수정/삭제 권한 없음"),

  // [COMMENT] 댓글 관리
  // 명세서 /api/comments 대응
  COMMENT_NOT_FOUND(404, "M401", "댓글 정보 없음"),
  NOT_COMMENT_OWNER(403, "M402", "댓글 수정/삭제 권한 없음"),
  COMMENT_BAD_REQUEST(400, "M403", "잘못된 요청 (리뷰 ID 누락 등)"),

  /*
   * [NOTIFICATION] 알림 관리
   * 명세서 /api/notifications 대응
   */
  NOTIFICATION_NOT_FOUND(404, "N501", "알림 정보 없음"),
  NOT_NOTIFICATION_OWNER(403, "N502", "알림 수정 권한 없음"),

  /*
   * [DASHBOARD] 대시보드
   * 명세서 /api/users/power, /api/reviews/popular 대응
   */
  INVALID_PERIOD(400, "D601", "잘못된 요청 (랭킹 기간 오류 등)")
  ;

  private final int status;
  private final String code;
  private final String message;
}