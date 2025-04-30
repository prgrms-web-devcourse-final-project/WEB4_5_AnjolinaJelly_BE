package com.jelly.zzirit.global.security.oauth2.service;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;
import com.jelly.zzirit.global.security.oauth2.info.OAuth2UserInfo;
import com.jelly.zzirit.global.security.oauth2.info.OAuth2UserInfoFactory;
import com.jelly.zzirit.global.security.oauth2.service.login.OAuthUserLoginService;
import com.jelly.zzirit.global.security.oauth2.service.signup.OAuthSignupRedirectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;
	private final OAuthUserLoginService oAuthUserLoginService;
	private final OAuthSignupRedirectService oAuthSignupRedirectService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		ProviderInfo provider = ProviderInfo.from(userRequest.getClientRegistration().getRegistrationId());

		log.info("[{}] OAuth2 Attributes: {}", provider.name(), oAuth2User.getAttributes());
		OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());

		if (oAuth2UserInfo.getEmail() == null) {
			throw new InvalidAuthenticationException(BaseResponseStatus.OAUTH_EMAIL_NOT_FOUND);
		}

		Optional<Member> userOpt = memberRepository.findByMemberEmail(oAuth2UserInfo.getEmail());
		if (userOpt.isPresent()) {
			return oAuthUserLoginService.restoreAndLinkOAuthUser(userOpt.get(), oAuth2UserInfo, provider);
		}

		return oAuthSignupRedirectService.handleNewUser(oAuth2UserInfo, provider);
	}
}