package com.jelly.zzirit.domain.member.dto.response;

import com.jelly.zzirit.domain.member.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyPageInfoDTO {

	private String memberName;
	private String memberAddress;
	private String memberAddressDetail;

	public static MyPageInfoDTO from(Member member) {
		return new MyPageInfoDTO(
			member.getMemberName(),
			member.getMemberAddress(),
			member.getMemberAddressDetail()
		);
	}
}