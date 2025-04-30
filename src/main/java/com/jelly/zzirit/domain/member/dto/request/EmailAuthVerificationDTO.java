package com.jelly.zzirit.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailAuthVerificationDTO {
	@NotNull(message = "이메일은 필수입니다.")
	@Email(message = "유효한 이메일을 입력해주세요.")
	private String email;

	@NotNull(message = "인증 코드는 필수입니다.")
	private String code;
}
