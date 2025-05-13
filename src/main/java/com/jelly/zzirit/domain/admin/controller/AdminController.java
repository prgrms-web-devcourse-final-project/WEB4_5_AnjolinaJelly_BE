package com.jelly.zzirit.domain.admin.controller;

import java.io.IOException;

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
import com.jelly.zzirit.domain.admin.dto.request.ItemUpdateRequest;
import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.admin.dto.response.ImageUploadResponse;
import com.jelly.zzirit.domain.admin.service.CommandAdminService;
import com.jelly.zzirit.domain.admin.service.QueryAdminService;
import com.jelly.zzirit.domain.admin.service.CommandS3Service;
import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.dto.response.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.dto.response.TimeDealFetchResponse;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.service.CommandTimeDealService;
import com.jelly.zzirit.domain.item.service.QueryTimeDealService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "관리자 상품 API", description = "관리자 상품 기능을 제공합니다.")
public class AdminController {
	private final QueryAdminService queryAdminItemService;
	private final CommandAdminService commandAdminItemService;
	private final QueryTimeDealService queryTimeDealService;
	private final CommandTimeDealService timeDealService;
	private final CommandS3Service commandS3Service;


	@Operation(summary = "관리자 상품 조회 & 검색", description = "관리자가 id/이름으로 상품 목록을 조회합니다.")
	@GetMapping("/items")
	public BaseResponse<PageResponse<AdminItemFetchResponse>> getItems(
		@RequestParam(required = false) String name,
		@RequestParam(required = false) Long itemId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		return BaseResponse.success(queryAdminItemService.getSearchItems(name, itemId, pageable));
	}

	@Operation(summary = "관리자 상품 이미지 업로드", description = "상품 등록 전 이미지를 S3에 업로드하고 URL 반환")
	@PostMapping("/items/image")
	public BaseResponse<ImageUploadResponse> uploadImage(@RequestPart("image") MultipartFile image) throws IOException {
		String uploadedUrl = commandS3Service.upload(image, "item-images");
		return BaseResponse.success(new ImageUploadResponse(uploadedUrl));
	}

	@Operation(summary = "관리자 상품 등록", description = "관리자가 상품을 등록합니다.")
	@PostMapping("/items")
	public BaseResponse<Empty> createItem(@RequestBody @Valid ItemCreateRequest request) {
		commandAdminItemService.createItem(request);
		return BaseResponse.success();
	}

	@Operation(summary = "관리자 상품 수정", description = "관리자가 id로 상품(재고, 가격)을 수정합니다.")
	@PutMapping("/items/{itemId}")
	public BaseResponse<Empty> updateItem(
		@PathVariable @NotNull Long itemId,
		@RequestBody @Valid ItemUpdateRequest request
	) {
		commandAdminItemService.updateItem(itemId, request);
		return BaseResponse.success();
	}
	
	@Operation(summary = "관리자 상품 이미지 수정", description = "상품 ID로 기존 상품의 이미지를 새 이미지로 교체")
	@PutMapping("/items/{itemId}/image")
	public BaseResponse<ImageUploadResponse> updateImage(
		@PathVariable Long itemId,
		@RequestPart("image") MultipartFile image
	) throws IOException {
		String uploadedUrl = commandS3Service.upload(image, "item-images");
		commandAdminItemService.updateImageUrl(itemId, uploadedUrl);
		return BaseResponse.success(new ImageUploadResponse(uploadedUrl));
	}

	@Operation(summary = "관리자 상품 삭제", description = "관리자가 id로 상품을 삭제합니다.")
	@DeleteMapping("/items/{itemId}")
	public BaseResponse<Empty> deleteItem(@PathVariable @NotNull Long itemId) {
		commandAdminItemService.deleteItem(itemId);
		return BaseResponse.success();
	}

	@Operation(summary = "타임딜 등록", description = "타임딜 정보와 아이템 리스트를 등록합니다.")
	@PostMapping("/time-deals")
	public BaseResponse<TimeDealCreateResponse> createTimeDeal(@RequestBody TimeDealCreateRequest request) {
		TimeDealCreateResponse response = timeDealService.createTimeDeal(request);
		return BaseResponse.success(response);
	}

	@GetMapping("/time-deals/search")
	@Operation(summary = "(관리자 페이지)타임딜 목록 조회", description = "관리자 페이지에서 타임딜 목록을 조회합니다.")
	public BaseResponse<PageResponse<TimeDealFetchResponse>> searchTimeDeals(
		@RequestParam(required = false) String timeDealName,
		@RequestParam(required = false) Long timeDealId,
		@RequestParam(required = false) String timeDealItemName,
		@RequestParam(required = false) Long timeDealItemId,
		@RequestParam(required = false) TimeDeal.TimeDealStatus status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		PageResponse<TimeDealFetchResponse> result = queryTimeDealService.getTimeDeals(
			timeDealName, timeDealId, timeDealItemName, timeDealItemId, status, page, size
		);
		return BaseResponse.success(result);
	}
}