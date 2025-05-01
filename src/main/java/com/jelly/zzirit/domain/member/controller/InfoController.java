package com.jelly.zzirit.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.security.oauth2.service.token.OAuthTempTokenService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/info")
@RequiredArgsConstructor
public class InfoController {

	private final OAuthTempTokenService oAuthTempTokenService;

	@GetMapping("/check")
	public void checkAuth() {
		log.info("유저 로그인 체크 : {}", AuthMember.getAuthUser());
	} // 현재 사용자의 인증 상태를 확인합니다

	@GetMapping("/temp-check")
	public void checkTempToken(HttpServletRequest request) {
		oAuthTempTokenService.extractTokenData(request);
	} // 임시 회원가입 절차 시, 임시 토큰의 만료여부 확인합니다
}