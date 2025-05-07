package com.jelly.zzirit.domain.member.service.userinfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.member.dto.request.MyPageAddressUpdateDTO;
import com.jelly.zzirit.domain.member.dto.response.MyPageInfoDTO;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {

	private final MemberRepository memberRepository;

	@Transactional(readOnly = true)
	public MyPageInfoDTO getMyPageInfo() {
		Member member = memberRepository.findById(AuthMember.getMemberId())
			.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));

		return MyPageInfoDTO.from(member);
	}

	@Transactional
	public void updateAddress(MyPageAddressUpdateDTO request) {
		Member member = memberRepository.findById(AuthMember.getMemberId())
			.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));

		member.updateAddress(request.getMemberAddress(), request.getMemberAddressDetail());
	}
}