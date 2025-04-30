package com.jelly.zzirit.global.security.oauth2.info;

import java.util.Map;

import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.global.security.oauth2.user.GoogleOAuth2User;
import com.jelly.zzirit.global.security.oauth2.user.KakaoOAuth2User;
import com.jelly.zzirit.global.security.oauth2.user.NaverOAuth2User;

public class OAuth2UserInfoFactory {
	public static OAuth2UserInfo getOAuth2UserInfo(ProviderInfo provider, Map<String, Object> attributes) {
		return switch (provider) {
			case NAVER -> new NaverOAuth2User(attributes);
			case GOOGLE -> new GoogleOAuth2User(attributes);
			case KAKAO -> new KakaoOAuth2User(attributes);
		};
	}
}