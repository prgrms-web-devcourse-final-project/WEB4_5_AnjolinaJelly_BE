package com.jelly.zzirit.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.member.dto.request.MyPageAddressUpdateDTO;
import com.jelly.zzirit.domain.member.dto.response.MyPageInfoDTO;
import com.jelly.zzirit.domain.member.service.userinfo.MyPageService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage")
@Tag(name = "마이페이지", description = "회원 마이페이지 API")
@SecurityRequirement(name = "accessToken")
public class MyPageController {

	private final MyPageService myPageService;

	@Operation(summary = "내 정보 조회", description = "회원의 이름 및 주소 정보를 조회합니다.")
	@GetMapping("/info")
	public BaseResponse<MyPageInfoDTO> getMyPageInfo() {
		MyPageInfoDTO info = myPageService.getMyPageInfo();
		return BaseResponse.success(info);
	}

	@Operation(summary = "주소 수정", description = "회원의 주소 및 상세주소를 수정합니다.")
	@PatchMapping("/address")
	public BaseResponse<Empty> updateAddress(@RequestBody @Valid MyPageAddressUpdateDTO request) {
		myPageService.updateAddress(request);
		return BaseResponse.success();
	}
}