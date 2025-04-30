package com.jelly.zzirit.global.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.exception.custom.InvalidCustomException;

import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCustomException.class)
	public BaseResponse<Empty> handleGlobalException(InvalidCustomException e, HttpServletResponse response) {
		response.setStatus(e.getStatus().getHttpStatus());
		return BaseResponse.error(e.getStatus());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public BaseResponse<List<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.toList());

		return BaseResponse.error(BaseResponseStatus.VALIDATION_FAILED, errors);
	} // 요청 DTO 검증 실패 [Validation 실패]
}