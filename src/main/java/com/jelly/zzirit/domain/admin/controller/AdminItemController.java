package com.jelly.zzirit.domain.admin.controller;

import java.io.IOException;

import com.jelly.zzirit.domain.admin.dto.request.ItemUpdateRequest;
import com.jelly.zzirit.domain.admin.service.CommandAdminItemService;
import com.jelly.zzirit.domain.admin.service.QueryAdminItemService;
import com.jelly.zzirit.domain.admin.service.S3Service;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jelly.zzirit.domain.admin.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.admin.dto.response.AdminItemResponse;
import com.jelly.zzirit.domain.admin.dto.response.ImageUploadResponse;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
@Tag(name = "관리자 상품 API", description = "관리자 상품 기능을 제공합니다.")
public class AdminItemController {
	private final QueryAdminItemService queryAdminItemService;
	private final CommandAdminItemService commandAdminItemService;
	private final S3Service s3Service;

	/**
	 * (관리자) 상품 조회 & 검색
	 */
	@Operation(summary = "관리자 상품 조회 & 검색", description = "관리자가 id/이름으로 상품 목록을 조회합니다.")	@GetMapping
	public BaseResponse<PageResponse<AdminItemResponse>> getItems(
			@RequestParam(required = false) Long itemId,
			@RequestParam(required = false) String name,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		return BaseResponse.success(queryAdminItemService.getSearchItems(itemId, name, pageable));
	}

	/**
	 * (관리자) 상품 이미지 등록
	 */
	@Operation(summary = "관리자 상품 이미지 업로드", description = "상품 등록 전 이미지를 S3에 업로드하고 URL 반환")
	@PostMapping("/image")
	public BaseResponse<ImageUploadResponse> uploadImage(@RequestPart("image") MultipartFile image) throws IOException {
		String uploadedUrl = s3Service.upload(image, "item-images");
		return BaseResponse.success(new ImageUploadResponse(uploadedUrl));
	}

	/**
	 * (관리자) 상품 등록, 이미지 URL 포함한 요청 본문 필요
	 */
	@Operation(summary = "관리자 상품 등록", description = "관리자가 상품을 등록합니다.")
	@PostMapping // validity check
	public BaseResponse<Empty> createItem(@RequestBody @Valid ItemCreateRequest request) {
		return BaseResponse.success(
				commandAdminItemService.createItem(request)
		);
	}

	/**
	 * (관리자) 상품 수정
	 */
	@Operation(summary = "관리자 상품 수정", description = "관리자가 id로 상품(재고, 가격)을 수정합니다.")
	@PutMapping("/{itemId}")
	public BaseResponse<Empty> updateItem(
		@PathVariable @NotNull Long itemId,
		@RequestBody @Valid ItemUpdateRequest request
	) {
		return BaseResponse.success(
			commandAdminItemService.updateItem(itemId, request)
		);
	}

	/**
	 * (관리자) 상품 이미지 수정
	 */
	@Operation(summary = "관리자 상품 이미지 수정", description = "상품 ID로 기존 상품의 이미지를 새 이미지로 교체")
	@PutMapping("/{itemId}/image")
	public BaseResponse<ImageUploadResponse> updateImage(
		@PathVariable Long itemId,
		@RequestPart("image") MultipartFile image
	) throws IOException {
		String uploadedUrl = s3Service.upload(image, "item-images");
		commandAdminItemService.updateImageUrl(itemId, uploadedUrl);
		return BaseResponse.success(new ImageUploadResponse(uploadedUrl));
	}

	/**
	 * (관리자) 상품 삭제
	 */
	@Operation(summary = "관리자 상품 삭제", description = "관리자가 id로 상품을 삭제합니다.")
	@DeleteMapping("/{itemId}")
	public BaseResponse<Empty> deleteItem(@PathVariable @NotNull Long itemId) {
		return BaseResponse.success(
				commandAdminItemService.deleteItem(itemId)
		);
	}
}