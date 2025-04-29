package com.jelly.zzirit.global.security.oauth2.info;

import java.util.Map;

import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class OAuth2UserInfo {

	protected final Map<String, Object> attributes;

	protected final ProviderInfo providerInfo;

	public String getProviderId() {
		return attributes.get(providerInfo.getProviderCode()).toString();
	}

	public String getEmail() {
		return attributes.get(providerInfo.getIdentifier()).toString();
	}
}