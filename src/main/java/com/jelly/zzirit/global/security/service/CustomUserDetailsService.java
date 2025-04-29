package com.jelly.zzirit.global.security.service;

import java.security.Principal;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;
import com.jelly.zzirit.global.security.model.MemberPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String memberEmail) throws UsernameNotFoundException {
		Member member = memberRepository.findByMemberEmail(memberEmail)
			.orElseThrow(() -> new InvalidAuthenticationException(BaseResponseStatus.USER_NOT_FOUND));

		return new MemberPrincipal(member.getId(), member.getPassword(), member.getRole());
	}
}
