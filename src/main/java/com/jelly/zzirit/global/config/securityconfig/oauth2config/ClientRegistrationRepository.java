package com.jelly.zzirit.global.config.securityconfig.oauth2config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ClientRegistrationRepository {

	private final SocialClientRegistration socialClientRegistration;

	public InMemoryClientRegistrationRepository clientRegistrationRepository() {
		return new InMemoryClientRegistrationRepository(
			socialClientRegistration.naverClientRegistration(),
			socialClientRegistration.googleClientRegistration());
	}
}