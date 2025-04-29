package com.jelly.zzirit.global.security.oauth2.service.login;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.OAuthMember;
import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.domain.member.mapper.MemberMapper;
import com.jelly.zzirit.domain.member.repository.OAuthMemberRepository;
import com.jelly.zzirit.global.security.model.MemberPrincipal;
import com.jelly.zzirit.global.security.oauth2.info.OAuth2UserInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthUserLoginService {

	private final OAuthMemberRepository oAuthMemberRepository;
	private final MemberMapper memberMapper;

	@Transactional
	public MemberPrincipal restoreAndLinkOAuthUser(Member existingUser, OAuth2UserInfo userInfo, ProviderInfo provider) {

		if (!oAuthMemberRepository.existsByMemberAndProvider(existingUser, provider)) {
			OAuthMember newOAuthUser = memberMapper.ofOAuthAccountForExistingMember(existingUser, userInfo, provider);
			oAuthMemberRepository.save(newOAuthUser);
		}

		return new MemberPrincipal(existingUser.getId(), existingUser.getPassword(), existingUser.getRole());
	}

	@Transactional
	public void cleanupFailedSignup(String email, ProviderInfo provider) {
		oAuthMemberRepository.findByUserEmailAndProvider(email, provider)
			.ifPresent(oAuthMemberRepository::delete);
	}
}