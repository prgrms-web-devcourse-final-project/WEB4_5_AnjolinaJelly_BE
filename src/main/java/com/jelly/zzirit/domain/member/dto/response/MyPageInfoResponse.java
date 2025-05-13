package com.jelly.zzirit.domain.member.dto.response;

import com.jelly.zzirit.domain.member.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyPageInfoResponse {

	private String memberName;
	private String memberAddress;
	private String memberAddressDetail;

	public static MyPageInfoResponse from(Member member) {
		return new MyPageInfoResponse(
			member.getMemberName(),
			member.getMemberAddress(),
			member.getMemberAddressDetail()
		);
	}
}