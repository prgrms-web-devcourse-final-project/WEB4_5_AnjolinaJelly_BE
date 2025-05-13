package com.jelly.zzirit.domain.member.mapper;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.member.dto.request.SignupRequest;
import com.jelly.zzirit.domain.member.dto.request.SocialSignupRequest;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.OAuthMember;
import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.security.oauth2.info.OAuth2UserInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberMapper {

	// 자체 회원가입 시 Member 변환
	public Member ofSignupDTO(SignupRequest signupRequest) {
		return Member.builder()
			.memberName(signupRequest.getMemberName())
			.memberEmail(signupRequest.getMemberEmail())
			.password(signupRequest.getMemberPassword())
			.role(Role.ROLE_USER)
			.memberAddress(signupRequest.getMemberAddress())
			.memberAddressDetail(signupRequest.getMemberAddressDetail())
			.build();
	}

	// 기존 회원이 추가 소셜 계정을 연동하는 경우
	public OAuthMember ofOAuthAccountForExistingMember(Member existingMember, OAuth2UserInfo oAuth2UserInfo, ProviderInfo provider) {
		return OAuthMember.builder()
			.member(existingMember)
			.provider(provider)
			.identifier(oAuth2UserInfo.getProviderId())
			.build();
	}

	// [자체 회원없이 소셜로 첫 회원가입] 소셜 로그인 이후 추가 정보 입력받아 Member 생성
	public Member ofSocialSignupDTO(SocialSignupRequest socialSignupRequest, String encodedPassword, String email) {
		return Member.builder()
			.memberName(socialSignupRequest.getMemberName())
			.memberEmail(email)
			.password(encodedPassword)
			.role(Role.ROLE_USER)
			.memberAddress(socialSignupRequest.getMemberAddress())
			.memberAddressDetail(socialSignupRequest.getMemberAddressDetail())
			.build();
	}

	// [자체 회원없이 소셜로 첫 회원가입] Member 회원가입 완료 후 OAuthMember 연동 정보 생성
	public OAuthMember ofOAuthSignupComplete(Member member, ProviderInfo provider, String providerId) {
		return OAuthMember.builder()
			.member(member)
			.provider(provider)
			.identifier(providerId)
			.build();
	}
}