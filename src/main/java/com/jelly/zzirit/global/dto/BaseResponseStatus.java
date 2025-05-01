package com.jelly.zzirit.global.dto;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

	// 요청 성공
	SUCCESS(true, 1000, "요청이 성공하였습니다.", OK),

	// 이메일 인증 관련
	EMAIL_REQUEST_LOCKED(false,2001, "인증 코드는 30초 후에 다시 요청할 수 있습니다.", BAD_REQUEST),
	EMAIL_ALREADY_VERIFIED(false,2002, "이메일 인증이 이미 완료되었습니다.", BAD_REQUEST),
	EMAIL_VERIFICATION_EXPIRED(false,2003, "이메일 인증 시간이 만료되었습니다. 다시 인증을 진행해 주세요.", BAD_REQUEST),
	EMAIL_VERIFICATION_REQUIRED(false,2004, "이메일 인증이 필요합니다.", BAD_REQUEST),
	EMAIL_INVALID_CODE(false,2005, "유효하지 않은 인증 코드입니다.", BAD_REQUEST),
	EMAIL_SEND_FAILED(false,2006, "이메일 전송에 실패했습니다.", INTERNAL_SERVER_ERROR),

	// 회원가입 관련
	USER_ALREADY_EXISTS(false,2101, "이미 가입된 이메일입니다. 다른 방법으로 로그인하세요.", CONFLICT),
	USER_PASSWORD_INVALID(false,2102, "비밀번호는 8~15자 이내로 숫자와 소문자를 포함해야 합니다.", BAD_REQUEST),
	USER_NICKNAME_DUPLICATE(false,2103, "이미 사용 중인 닉네임입니다.", CONFLICT),
	USER_RECOVERY_REQUIRED(false,2104, "이 계정은 삭제되었습니다. 로그인 하셔서 계정 복구하시길 바랍니다", FORBIDDEN),
	USER_NOT_FOUND(false,2105, "사용자를 찾을 수 없습니다.", NOT_FOUND),
	TEMP_USER_NOT_FOUND(false,2106, "임시 사용자 정보를 찾을 수 없습니다.", NOT_FOUND),
	USER_NICKNAME_NOT_VERIFIED(false,2107, "닉네임 중복 검사를 먼저 진행하세요.", BAD_REQUEST),
	USER_TEMP_SESSION_EXPIRED(false,2108, "임시 세션이 만료되었습니다. 다시 소셜 로그인을 진행하세요.", BAD_REQUEST),
	USER_SOCIAL_SIGNUP_REQUIRED(false,2109, "추가 정보 입력이 필요합니다. 닉네임과 비밀번호를 설정해주세요.", HttpStatus.UNAUTHORIZED),
	USER_PASSWORD_NOT_VALID(false,2110, "비밀번호 형식이 올바르지 않습니다.", BAD_REQUEST),

	// TEMP_AUTHORIZATION_HEADER 관련
	TEMP_AUTHORIZATION_HEADER_MISSING(false,2201, "임시 인증 토큰이 없습니다.", HttpStatus.UNAUTHORIZED),
	TEMP_AUTHORIZATION_HEADER_INVALID(false,2202, "유효하지 않은 임시 인증 토큰입니다.", HttpStatus.UNAUTHORIZED),
	TEMP_AUTHORIZATION_HEADER_EXPIRED(false,2203, "임시 인증 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),

	// OAuth 관련
	OAUTH_USER_NOT_FOUND(false,2201, "소셜 로그인 사용자 정보를 찾을 수 없습니다.", NOT_FOUND),
	OAUTH_REDIRECT_FAILED(false,2202, "소셜 로그인 후 리다이렉트에 실패했습니다.", INTERNAL_SERVER_ERROR),
	OAUTH_EMAIL_NOT_FOUND(false, 2203, "이메일 정보를 가져올 수 없습니다.", BAD_REQUEST),

	// 회원 관련
	INVALID_PASSWORD(false,2301, "비밀번호가 일치하지 않습니다", BAD_REQUEST),

	// 실패
	VALIDATION_FAILED(false,40000, "입력 값이 유효하지 않습니다", BAD_REQUEST),
	AUTH_REQUEST_BODY_INVALID(false,40001, "잘못된 요청 본문입니다.", BAD_REQUEST),


	// 인증 & 인가
	UNAUTHORIZED(false,40002, "인증되지 않은 요청입니다.", HttpStatus.UNAUTHORIZED),
	ACCESS_DENIED(false,40003, "접근 권한이 없습니다.", FORBIDDEN),
	AUTH_CHECK_FAILED(false, 40004, "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
	NO_AUTHORITY(false, 40005, "수행할 권한이 없습니다.", FORBIDDEN),

	// JWT 관련 예외
	JWT_BLACKLISTED(false,40010, "블랙리스트에 등록된 토큰입니다.", HttpStatus.UNAUTHORIZED),
	JWT_INVALID(false,40011, "잘못된 토큰입니다.", HttpStatus.UNAUTHORIZED),
	JWT_MISSING(false,40012, "토큰이 존재하지 않습니다", HttpStatus.UNAUTHORIZED),

	// RefreshToken 관련 예외
	REFRESH_TOKEN_NULL(false,40020, "리프레시 토큰이 없습니다.", HttpStatus.UNAUTHORIZED),
	REFRESH_TOKEN_EXPIRED(false,40021, "리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
	REFRESH_TOKEN_INVALID(false,40022, "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
	REFRESH_TOKEN_NOT_FOUND(false,40023, "저장된 리프레시 토큰이 없습니다.", HttpStatus.UNAUTHORIZED),

	// ResponseWrapper 관련 예외
	NOT_SUPPORTED(false, 50001, "지원하지 않는 형식입니다.", INTERNAL_SERVER_ERROR)
	;


	private final boolean isSuccess;
	private final int code;
	private final String message;
	private final HttpStatus httpStatus;

	BaseResponseStatus(boolean isSuccess , int code, String message, HttpStatus httpStatus) {
		this.isSuccess = isSuccess;
		this.code = code;
		this.message = message;
		this.httpStatus = httpStatus;
	}

	public boolean isSuccess() {
		return isSuccess;
	}
}