package com.jelly.zzirit.global.security.oauth2.service.signup;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.global.security.oauth2.info.OAuth2UserInfo;

@Service
public class OAuthUserTempService {

	private final RedisTemplate<String, Object> jsonRedisTemplate;

	public OAuthUserTempService(@Qualifier("TokenJsonRedisTemplate") RedisTemplate<String, Object> jsonRedisTemplate) {
		this.jsonRedisTemplate = jsonRedisTemplate;
	}

	private static final Duration TEMP_OAUTH_EXPIRATION = Duration.ofMinutes(10);

	public void saveTempOAuthUser(OAuth2UserInfo oAuth2UserInfo, ProviderInfo provider, String tempToken) {
		Map<String, String> tempData = Map.of(
			"email", oAuth2UserInfo.getEmail(),
			"provider", provider.name(),
			"providerId", oAuth2UserInfo.getProviderId(),
			"tempToken", tempToken
		);

		jsonRedisTemplate.opsForValue().set(
			"temp_oauth_user:" + oAuth2UserInfo.getEmail(),
			tempData,
			TEMP_OAUTH_EXPIRATION
		);
	}

	public void deleteTempOAuthUser(String email) {
		jsonRedisTemplate.delete("temp_oauth_user:" + email);
	}
}