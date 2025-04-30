package com.jelly.zzirit.global.config.securityconfig.oauth2config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialClientRegistration {

	@Value("${oauth2.naver.client-id}")
	private String naverClientId;

	@Value("${oauth2.naver.client-secret}")
	private String naverClientSecret;

	@Value("${oauth2.naver.redirect-uri}")
	private String naverRedirectUri;

	@Value("${oauth2.google.client-id}")
	private String googleClientId;

	@Value("${oauth2.google.client-secret}")
	private String googleClientSecret;

	@Value("${oauth2.google.redirect-uri}")
	private String googleRedirectUri;

	@Value("${oauth2.kakao.client-id}")
	private String kakaoClientId;

	@Value("${oauth2.kakao.redirect-uri}")
	private String kakaoRedirectUri;

	public ClientRegistration naverClientRegistration() {
		return ClientRegistration.withRegistrationId("naver")
			.clientId(naverClientId)
			.clientSecret(naverClientSecret)
			.redirectUri(naverRedirectUri)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.scope("email")
			.authorizationUri("https://nid.naver.com/oauth2.0/authorize")
			.tokenUri("https://nid.naver.com/oauth2.0/token")
			.userInfoUri("https://openapi.naver.com/v1/nid/me")
			.userNameAttributeName("response")
			.build();
	}

	public ClientRegistration googleClientRegistration() {
		return ClientRegistration.withRegistrationId("google")
			.clientId(googleClientId)
			.clientSecret(googleClientSecret)
			.redirectUri(googleRedirectUri)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.scope("email", "profile")
			.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
			.tokenUri("https://www.googleapis.com/oauth2/v4/token")
			.jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
			.issuerUri("https://accounts.google.com")
			.userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
			.userNameAttributeName(IdTokenClaimNames.SUB)
			.build();
	}

	public ClientRegistration kakaoClientRegistration() {
		return ClientRegistration.withRegistrationId("kakao")
			.clientId(kakaoClientId)
			.redirectUri(kakaoRedirectUri)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.scope("profile_nickname", "account_email")
			.authorizationUri("https://kauth.kakao.com/oauth/authorize")
			.tokenUri("https://kauth.kakao.com/oauth/token")
			.userInfoUri("https://kapi.kakao.com/v2/user/me")
			.userNameAttributeName("id")
			.build();
	}
}