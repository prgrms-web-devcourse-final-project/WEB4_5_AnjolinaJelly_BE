package com.jelly.zzirit.global.security.oauth2.user;

import java.util.Map;

import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.global.security.oauth2.info.OAuth2UserInfo;

public class KakaoOAuth2User extends OAuth2UserInfo {

	public KakaoOAuth2User(Map<String, Object> attributes) {
		super(attributes, ProviderInfo.KAKAO);
	}

	@Override
	public String getEmail() {
		Object accountObj = attributes.get("kakao_account");

		if (accountObj instanceof Map accountMap) {
			Object email = accountMap.get("email");
			return email != null ? email.toString() : null;
		}
		return null;
	}
}