package com.jelly.zzirit.global.dto;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

	// 요청 성공
	SUCCESS(true, 1000, "요청이 성공하였습니다.", 200),

	// 이메일 인증 관련
	EMAIL_REQUEST_LOCKED(false,2001, "인증 코드는 30초 후에 다시 요청할 수 있습니다.", 400),
	EMAIL_ALREADY_VERIFIED(false,2002, "이메일 인증이 이미 완료되었습니다.", 400),
	EMAIL_VERIFICATION_EXPIRED(false,2003, "이메일 인증 시간이 만료되었습니다. 다시 인증을 진행해 주세요.", 400),
	EMAIL_VERIFICATION_REQUIRED(false,2004, "이메일 인증이 필요합니다.", 400),
	EMAIL_INVALID_CODE(false,2005, "유효하지 않은 인증 코드입니다.", 400),
	EMAIL_SEND_FAILED(false,2006, "이메일 전송에 실패했습니다.", 500),

	// 회원가입 관련
	USER_ALREADY_EXISTS(false,2101, "이미 가입된 이메일입니다. 다른 방법으로 로그인하세요.", 409),
	USER_PASSWORD_INVALID(false,2102, "비밀번호는 8~15자 이내로 숫자와 소문자를 포함해야 합니다.", 400),
	USER_NICKNAME_DUPLICATE(false,2103, "이미 사용 중인 닉네임입니다.", 409),
	USER_RECOVERY_REQUIRED(false,2104, "이 계정은 삭제되었습니다. 로그인 하셔서 계정 복구하시길 바랍니다", 403),
	USER_NOT_FOUND(false,2105, "사용자를 찾을 수 없습니다.", 404),
	TEMP_USER_NOT_FOUND(false,2106, "임시 사용자 정보를 찾을 수 없습니다.", 404),
	USER_NICKNAME_NOT_VERIFIED(false,2107, "닉네임 중복 검사를 먼저 진행하세요.", 400),
	USER_TEMP_SESSION_EXPIRED(false,2108, "임시 세션이 만료되었습니다. 다시 소셜 로그인을 진행하세요.", 400),
	USER_SOCIAL_SIGNUP_REQUIRED(false,2109, "추가 정보 입력이 필요합니다. 닉네임과 비밀번호를 설정해주세요.", 401),
	USER_PASSWORD_NOT_VALID(false,2110, "비밀번호 형식이 올바르지 않습니다.", 400),

	// TEMP_AUTHORIZATION_HEADER 관련
	TEMP_AUTHORIZATION_HEADER_MISSING(false,2201, "임시 인증 토큰이 없습니다.", 401),
	TEMP_AUTHORIZATION_HEADER_INVALID(false,2202, "유효하지 않은 임시 인증 토큰입니다.", 401),
	TEMP_AUTHORIZATION_HEADER_EXPIRED(false,2203, "임시 인증 토큰이 만료되었습니다.", 401),

	// OAuth 관련
	OAUTH_USER_NOT_FOUND(false,2201, "소셜 로그인 사용자 정보를 찾을 수 없습니다.", 404),
	OAUTH_REDIRECT_FAILED(false,2202, "소셜 로그인 후 리다이렉트에 실패했습니다.", 500),

	// 회원 관련
	INVALID_PASSWORD(false,2301, "비밀번호가 일치하지 않습니다", 400),

	// 실패
	VALIDATION_FAILED(false,40000, "입력 값이 유효하지 않습니다", 400),
	AUTH_REQUEST_BODY_INVALID(false,40001, "잘못된 요청 본문입니다.", 400),


	// 인증 & 인가
	UNAUTHORIZED(false,40002, "인증되지 않은 요청입니다.", 401),
	ACCESS_DENIED(false,40003, "접근 권한이 없습니다.", 403),
	AUTH_CHECK_FAILED(false, 40004, "로그인이 필요합니다.", 401),
	NO_AUTHORITY(false, 40005, "수행할 권한이 없습니다.", 403),

	// JWT 관련 예외
	JWT_BLACKLISTED(false,40010, "블랙리스트에 등록된 토큰입니다.", 401),
	JWT_INVALID(false,40011, "잘못된 토큰입니다.", 401),
	JWT_MISSING(false,40012, "토큰이 존재하지 않습니다", 401),

	// RefreshToken 관련 예외
	REFRESH_TOKEN_NULL(false,40020, "리프레시 토큰이 없습니다.", 401),
	REFRESH_TOKEN_EXPIRED(false,40021, "리프레시 토큰이 만료되었습니다.", 401),
	REFRESH_TOKEN_INVALID(false,40022, "유효하지 않은 리프레시 토큰입니다.", 401),
	REFRESH_TOKEN_NOT_FOUND(false,40023, "저장된 리프레시 토큰이 없습니다.", 401);


	private final boolean isSuccess;
	private final int code;
	private final String message;
	private final int httpStatus;

	BaseResponseStatus(boolean isSuccess , int code, String message, int httpStatus) {
		this.isSuccess = isSuccess;
		this.code = code;
		this.message = message;
		this.httpStatus = httpStatus;
	}

	public boolean isSuccess() {
		return isSuccess;
	}
}