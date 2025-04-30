package com.jelly.zzirit.domain.member.util;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordManager {

	private final PasswordValidation passwordValidation;

	public boolean isInvalid(String password) {
		return !passwordValidation.isValid(password);
	}
}