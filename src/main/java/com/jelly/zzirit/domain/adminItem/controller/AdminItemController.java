package com.jelly.zzirit.domain.adminItem.controller;

import com.jelly.zzirit.domain.adminItem.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.adminItem.dto.response.AdminItemResponse;
import com.jelly.zzirit.domain.adminItem.dto.response.ImageUploadResponse;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/item")
@RequiredArgsConstructor
@Tag(name = "AdminItem API", description = "상품 관련 관리자 기능을 제공합니다.")
public class AdminItemController {

    /**
     * (관리자) 상품 조회 & 검색
     */
    @Operation(summary = "관리자 상품 조회 & 검색", description = "관리자가 id/이름으로 상품 목록을 조회합니다.")
    @GetMapping
    public BaseResponse<List<AdminItemResponse>> searchItems(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long itemId
    ) {
        List<AdminItemResponse> dummyItems = List.of(
                new AdminItemResponse(1L, "iphone 15 pro", "https://dummyimage.com/iphone.jpg", 50, "휴대폰", "애플", 1_200_000),
                new AdminItemResponse(2L, "갤럭시북3", "https://dummyimage.com/galaxybook.jpg", 100, "노트북", "삼성", 790_000)
        );
        return BaseResponse.success(dummyItems);
    }

    /**
     * (관리자) 상품 등록
     */
    @Operation(summary = "관리자 상품 등록", description = "관리자가 상품을 등록합니다.")
    @PostMapping
    public BaseResponse<Empty> addItem(@RequestBody ItemCreateRequest request) {
        return BaseResponse.success();
    }

    /**
     * (관리자) 상품 수정
     */
    @Operation(summary = "관리자 상품 수정", description = "관리자가 id로 상품을 수정합니다.")
    @PutMapping("/{itemId}")
    public BaseResponse<Empty> updateItem(@PathVariable Long itemId, @RequestBody ItemCreateRequest request) {
        return BaseResponse.success();
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
    public BaseResponse<Empty> deleteItem(@PathVariable Long itemId) {
        return BaseResponse.success();
    }
}