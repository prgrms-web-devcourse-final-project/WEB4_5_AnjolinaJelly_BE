package com.jelly.zzirit.global.security.oauth2.service.signup;

import java.util.Objects;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;
import com.jelly.zzirit.global.security.model.MemberPrincipal;
import com.jelly.zzirit.global.security.oauth2.info.OAuth2UserInfo;
import com.jelly.zzirit.global.security.oauth2.service.login.OAuthUserLoginService;
import com.jelly.zzirit.global.security.oauth2.service.token.TempTokenService;
import com.jelly.zzirit.global.security.util.AuthConst;
import com.jelly.zzirit.global.security.util.CookieUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthSignupRedirectService {

	private final TempTokenService tempTokenService;
	private final OAuthUserTempService oAuthUserTempService;
	private final OAuthUserLoginService oAuthUserLoginService;

	public OAuth2User handleNewUser(OAuth2UserInfo oAuth2UserInfo, ProviderInfo provider) {
		String tempToken = tempTokenService.createTempSignupToken(
			oAuth2UserInfo.getEmail(),
			provider.name(),
			oAuth2UserInfo.getProviderId(),
			600_000L
		);

		oAuthUserTempService.saveTempOAuthUser(oAuth2UserInfo, provider, tempToken);

		if (!sendTempTokenAndRedirect(tempToken)) {
			oAuthUserLoginService.cleanupFailedSignup(oAuth2UserInfo.getEmail(), provider);
			throw new InvalidAuthenticationException(BaseResponseStatus.OAUTH_REDIRECT_FAILED);
		}

		MemberPrincipal memberPrincipal = new MemberPrincipal(0L, Role.ROLE_GUEST);
		Authentication authentication = new UsernamePasswordAuthenticationToken(memberPrincipal, null, memberPrincipal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return memberPrincipal;
	}

	private boolean sendTempTokenAndRedirect(String tempToken) {
		HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
		if (response == null) return false;
		if (!response.isCommitted()) {
			response.addCookie(CookieUtil.createCookie(AuthConst.TOKEN_TYPE_TEMP, tempToken, 10 * 60));
		}
		return true;
	}
}