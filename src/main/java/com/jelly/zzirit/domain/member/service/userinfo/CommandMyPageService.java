package com.jelly.zzirit.domain.member.service.userinfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.member.dto.request.AddressUpdateRequest;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandMyPageService {

	private final MemberRepository memberRepository;

	@Transactional
	public void updateAddress(AddressUpdateRequest request) {
		Member member = memberRepository.findById(AuthMember.getMemberId())
			.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));

		member.updateAddress(request.getMemberAddress(), request.getMemberAddressDetail());
	}
}
