package com.jelly.zzirit.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddressUpdateRequest {

	@NotBlank(message = "주소는 필수 항목입니다.")
	@Size(min = 2, max = 100, message = "주소는 2자 이상 100자 이하여야 합니다.")
	private String memberAddress;

	@Size(max = 100, message = "상세주소는 100자 이하여야 합니다.")
	private String memberAddressDetail;
}