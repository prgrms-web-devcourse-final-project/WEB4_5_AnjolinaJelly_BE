package com.jelly.zzirit.domain.member.util;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidation {

	private static final Pattern PASSWORD_PATTERN = Pattern.compile(
			"^(?=.*[0-9])" + // 숫자 포함
			"(?=.*[a-z])" +  // 소문자 포함
			"(?=\\S+$).{8,15}$" // 공백 미포함 및 8글자 이상 15글자 이하
	);

	public boolean isValid(String password) {
		return password != null && PASSWORD_PATTERN.matcher(password).matches();
	}
}