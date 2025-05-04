package com.jelly.zzirit.domain.adminItem.controller;

import java.util.List;

import com.jelly.zzirit.domain.adminItem.service.CommandAdminItemService;
import com.jelly.zzirit.domain.adminItem.service.QueryAdminItemService;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

import com.jelly.zzirit.domain.adminItem.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.adminItem.dto.response.AdminItemResponse;
import com.jelly.zzirit.domain.adminItem.dto.response.ImageUploadResponse;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/item")
@RequiredArgsConstructor
@Tag(name = "관리자 상품 API", description = "관리자 상품 기능을 제공합니다.")
public class AdminItemController {
	private final QueryAdminItemService queryAdminItemService;
	private final CommandAdminItemService commandAdminItemService;

	/**
	 * (관리자) 상품 조회 & 검색
	 */
	@Operation(summary = "관리자 상품 조회 & 검색", description = "관리자가 id/이름으로 상품 목록을 조회합니다.")
	@GetMapping
	public BaseResponse<List<AdminItemResponse>> getItems(
		@RequestParam(required = false) String name,
		@RequestParam(required = false) Long itemId
	) {
		return BaseResponse.success(queryAdminItemService.getItems());
	}

	/**
	 * (관리자) 상품 등록
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
	@Operation(summary = "관리자 상품 수정", description = "관리자가 id로 상품을 수정합니다.")
	@PutMapping("/{itemId}")
	public BaseResponse<Empty> updateItem(@PathVariable @NotNull Long itemId, @RequestBody @Valid ItemCreateRequest request) {
		return BaseResponse.success(
				commandAdminItemService.updateItem(itemId, request)
		);
	}

	/**
	 * (관리자) 상품 이미지 업로드
	 */
	@Operation(summary = "관리자 상품 이미지  업로드", description = "관리자가 id로 상품 이미지를 업로드합니다.")
	@PostMapping("/{itemId}/image")
	public BaseResponse<ImageUploadResponse> uploadImage(@PathVariable Long itemId,
		@RequestPart("image") MultipartFile image) {
		//        // 1. S3 업로드
		//        String uploadedUrl = s3Uploader.upload(image, "item-images"); // 예: https://bucket.s3.amazonaws.com/item-images/uuid.jpg
		//
		//        // 2. DB 업데이트
		//        itemService.updateImageUrl(itemId, uploadedUrl);
		return BaseResponse.success(new ImageUploadResponse("https://dummyimage.com/iphone.jpg"));
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