package com.jelly.zzirit.domain.item.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.timeDeal.TimeDealModalItem;
import com.jelly.zzirit.domain.item.dto.timeDeal.response.SearchTimeDeal;
import com.jelly.zzirit.domain.item.dto.timeDeal.response.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.service.TimeDealService;
import com.jelly.zzirit.domain.timeDeal.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "타임딜 상품 API", description = "타임딜 기능을 제공합니다.")
public class TimeDealController {
	private final TimeDealService timeDealService;

	@Operation(summary = "타임딜 등록", description = "타임딜 정보와 아이템 리스트를 등록합니다.")
	@PostMapping("/api/admin/time-deal")
	public BaseResponse<TimeDealCreateResponse> createTimeDeal(@RequestBody TimeDealCreateRequest request) {
		TimeDealCreateResponse response = timeDealService.createTimeDeal(request);
		return BaseResponse.success(response);
	}

	@PostMapping("/api/admin/time-deal/modal")
	@Operation(summary = "타임딜 생성 모달 상품 조회")
	public BaseResponse<List<TimeDealModalItem>> getTimeDealModalItems(@RequestBody List<Long> itemIds) {
		List<TimeDealModalItem> result = timeDealService.getModalItems(itemIds);
		return BaseResponse.success(result);
	}

	@GetMapping("/api/time-deal/now")
	@Operation(
		summary = "현재 진행 중인 타임딜",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "현재 진행 중인 타임딜 예시",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = "{\n" +
							"  \"title\": \"노트북 90% 할인\",\n" +
							"  \"startTime\": \"2025-05-01T00:00:00\",\n" +
							"  \"endTime\": \"2025-05-01T12:00:00\",\n" +
							"  \"discountRate\": 90,\n" +
							"  \"status\": \"ONGOING\",\n" +
							"  \"items\": [\n" +
							"    {\n" +
							"      \"itemId\": 1,\n" +
							"      \"imageUrl\": \"https://example.com/item1.jpg\",\n" +
							"      \"originalPrice\": 1000000,\n" +
							"      \"finalPrice\": 100000,\n" +
							"      \"type\": \"노트북\",\n" +
							"      \"brand\": \"애플\"\n" +
							"    },\n" +
							"    {\n" +
							"      \"itemId\": 2,\n" +
							"      \"imageUrl\": \"https://example.com/item2.jpg\",\n" +
							"      \"originalPrice\": 800000,\n" +
							"      \"finalPrice\": 80000,\n" +
							"      \"type\": \"노트북\",\n" +
							"      \"brand\": \"삼성\"\n" +
							"    }\n" +
							"  ]\n" +
							"}"
					)
				)
			)
		}
	)
	public BaseResponse<Map<String, Object>> getCurrentTimeDeals() {
		Map<String, Object> timeDeal = Map.of(
			"title", "노트북 90% 할인",
			"startTime", "2025-05-01T00:00:00",
			"endTime", "2025-05-01T12:00:00",
			"discountRate", 90,
			"status", "ONGOING",
			"items", List.of(
				Map.of(
					"itemId", 1,
					"imageUrl", "https://example.com/item1.jpg",
					"originalPrice", 1000000,
					"finalPrice", 100000,
					"type", "노트북",
					"brand", "애플"
				),
				Map.of(
					"itemId", 2,
					"imageUrl", "https://example.com/item2.jpg",
					"originalPrice", 800000,
					"finalPrice", 80000,
					"type", "노트북",
					"brand", "삼성"
				)
			)
		);
		return BaseResponse.success(timeDeal);
	}

	@GetMapping("/api/time-deal/search")
	@Operation(summary = "(관리자 페이지)타임딜 목록 조회", description = "관리자 페이지에서 타임딜 목록을 조회합니다.")
	public ResponseEntity<PageResponse<SearchTimeDeal>> searchTimeDeals(
		@RequestParam(required = false) String timeDealName,
		@RequestParam(required = false) Long timeDealId,
		@RequestParam(required = false) String timeDealItemName,
		@RequestParam(required = false) Long timeDealItemId,
		@RequestParam(required = false) TimeDeal.TimeDealStatus status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		PageResponse<SearchTimeDeal> result = timeDealService.getTimeDeals(
			timeDealName, timeDealId, timeDealItemName, timeDealItemId, status, page, size
		);
		return ResponseEntity.ok(result);
	}
}

