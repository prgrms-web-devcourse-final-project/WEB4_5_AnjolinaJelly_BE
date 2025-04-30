package com.jelly.zzirit.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignupDTO {

	@NotBlank(message = "이름은 필수 항목입니다.")
	@Size(min = 2, max = 10, message = "이름은 2자 이상, 10자 이하로 입력해주세요.")
	private String memberName;

	@Email(message = "잘못된 이메일 형식입니다.")
	@NotBlank(message = "이메일은 필수 항목입니다.")
	@Size(max = 50, message = "이메일은 50자 이하로 입력해주세요.")
	private String memberEmail;

	@NotBlank(message = "비밀번호는 필수 항목입니다.")
	@Size(min = 8, max = 15, message = "비밀번호는 8~15자 이내로 숫자와 소문자를 포함해야 합니다.")
	private String memberPassword;

	@Size(max = 255, message = "주소는 255자 이하로 입력해주세요.")
	private String memberAddress;

	@Size(max = 255, message = "상세 주소는 255자 이하로 입력해주세요.")
	private String memberAddressDetail;
}