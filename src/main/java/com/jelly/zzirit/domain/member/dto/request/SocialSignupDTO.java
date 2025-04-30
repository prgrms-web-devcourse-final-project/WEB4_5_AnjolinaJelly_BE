package com.jelly.zzirit.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SocialSignupDTO {

	@NotBlank(message = "이름은 필수 항목입니다.")
	@Size(min = 2, max = 10, message = "이름은 2자 이상, 10자 이하로 입력해주세요.")
	private String memberName;

	@Size(max = 255, message = "주소는 255자 이하로 입력해주세요.")
	private String memberAddress;

	@Size(max = 255, message = "상세 주소는 255자 이하로 입력해주세요.")
	private String memberAddressDetail;

	@NotBlank(message = "비밀번호는 필수 항목입니다.")
	@Size(min = 8, max = 15, message = "비밀번호는 8자 이상 15자 이하로 입력해주세요.")
	private String memberPassword;
}