package com.jelly.zzirit.global.security.oauth2.user;

import java.util.Map;

import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.global.security.oauth2.info.OAuth2UserInfo;

public class GoogleOAuth2User extends OAuth2UserInfo {
	public GoogleOAuth2User(Map<String, Object> attributes) {
		super(attributes, ProviderInfo.GOOGLE);
	}

	@Override
	public String getEmail() {
		Object email = attributes.get("email");
		return email != null ? email.toString() : null;
	}
}