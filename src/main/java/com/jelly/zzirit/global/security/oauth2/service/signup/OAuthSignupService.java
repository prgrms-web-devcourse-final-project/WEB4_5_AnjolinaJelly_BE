package com.jelly.zzirit.global.security.oauth2.service.signup;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.member.dto.request.SocialSignupRequest;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.OAuthMember;
import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.domain.member.mapper.MemberMapper;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.domain.member.repository.OAuthMemberRepository;
import com.jelly.zzirit.domain.member.util.PasswordManager;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;
import com.jelly.zzirit.global.security.util.AuthConst;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthSignupService {

	private final MemberRepository memberRepository;
	private final OAuthMemberRepository oAuthMemberRepository;
	private final MemberMapper memberMapper;
	private final PasswordManager passwordManager;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public Member processSignup(SocialSignupRequest socialSignupRequest, Map<String, String> tokenData) {
		String encodedPassword = passwordEncoder.encode(socialSignupRequest.getMemberPassword());

		if (!passwordManager.isInvalid(encodedPassword)) {
			throw new InvalidUserException(BaseResponseStatus.USER_PASSWORD_NOT_VALID);
		}

		Member member = createUserEntity(socialSignupRequest, tokenData, encodedPassword);
		saveOAuthUser(member, tokenData);
		return member;
	}

	private Member createUserEntity(SocialSignupRequest socialSignupRequest, Map<String, String> tokenData, String encodedPassword) {
		String email = tokenData.get(AuthConst.TEMP_USER_EMAIL);
		return memberRepository.save(memberMapper.ofSocialSignupDTO(socialSignupRequest, encodedPassword, email));
	}

	private void saveOAuthUser(Member member, Map<String, String> tokenData) {
		ProviderInfo provider = ProviderInfo.valueOf(tokenData.get(AuthConst.TEMP_PROVIDER));
		String providerId = tokenData.get(AuthConst.TEMP_PROVIDER_ID);
		OAuthMember newOAuthUser = memberMapper.ofOAuthSignupComplete(member, provider, providerId);
		oAuthMemberRepository.save(newOAuthUser);
	}
}