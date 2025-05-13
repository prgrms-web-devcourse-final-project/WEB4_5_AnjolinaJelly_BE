package com.jelly.zzirit.global.security.oauth2.service.signup;

import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.member.dto.request.SocialSignupRequest;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.security.model.MemberPrincipal;
import com.jelly.zzirit.global.security.oauth2.service.token.OAuthTempTokenService;
import com.jelly.zzirit.global.security.util.AuthConst;
import com.jelly.zzirit.global.security.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FirstOAuthSignUpService {

	private final OAuthSignupService oAuthSignupService;
	private final OAuthTempTokenService oAuthTempTokenService;
	private final OAuthUserTempService oAuthUserTempService;

	@Transactional
	public void finalizeSocialSignup(HttpServletRequest request, HttpServletResponse response, SocialSignupRequest socialSignupRequest) {
		Map<String, String> tokenData = oAuthTempTokenService.extractTokenData(request);
		Member newUser = oAuthSignupService.processSignup(socialSignupRequest, tokenData);
		MemberPrincipal memberPrincipal = new MemberPrincipal(newUser.getId(), newUser.getRole());

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			memberPrincipal,
			null,
			memberPrincipal.getAuthorities()
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		oAuthTempTokenService.generateAndSetTokens(response, newUser);
		oAuthUserTempService.deleteTempOAuthUser(tokenData.get(AuthConst.TEMP_USER_EMAIL));
		response.addCookie(CookieUtil.deleteCookie(AuthConst.TOKEN_TYPE_TEMP));
	}
}
