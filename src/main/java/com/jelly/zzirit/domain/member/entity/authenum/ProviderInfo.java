package com.jelly.zzirit.domain.member.entity.authenum;

import java.util.Arrays;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProviderInfo {
	NAVER("response", "id", "email"),
	GOOGLE(null, "sub", "email");

	private final String attributeKey; // 응답 형식의 맨 상위 필드
	private final String providerCode; // 각 소셜은 판별하는 판별 코드
	private final String identifier;   // 소셜로그인을 한 사용자의 정보를 불러올 때 필요한 Key

	public static ProviderInfo from(String provider) {
		return Arrays.stream(ProviderInfo.values())
			.filter(item -> item.name().equalsIgnoreCase(provider))
			.findFirst()
			.orElseThrow(() -> new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다."));
	}
}