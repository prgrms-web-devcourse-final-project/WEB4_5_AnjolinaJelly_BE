package com.jelly.zzirit.global.security.oauth2.user;

import java.util.Map;

import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.global.security.oauth2.info.OAuth2UserInfo;

@SuppressWarnings("unchecked")
public class NaverOAuth2User extends OAuth2UserInfo {
	public NaverOAuth2User(Map<String, Object> attributes) {
		super((Map<String, Object>) attributes.get("response"), ProviderInfo.NAVER);
	}

	@Override
	public String getEmail() {
		Object email = attributes.get("email");
		return email != null ? email.toString() : null;
	}
}