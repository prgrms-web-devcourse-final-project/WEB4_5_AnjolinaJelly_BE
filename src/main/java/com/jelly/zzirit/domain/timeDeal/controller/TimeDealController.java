package com.jelly.zzirit.domain.timeDeal.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.timeDeal.dto.response.TimeDealCreateResponse;
import com.jelly.zzirit.domain.timeDeal.dto.response.TimeDealModalItem;
import com.jelly.zzirit.domain.timeDeal.entity.TimeDealCreateItem;
import com.jelly.zzirit.global.dto.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "타임딜 API", description = "타임딜 관련 API를 설명합니다.")
public class TimeDealController {
	@Operation(
		summary = "타임딜 등록",
		description = "타임딜 정보와 아이템 리스트를 등록합니다.",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					name = "타임딜 등록 요청 예시",
					value = "{\n" +
						"  \"title\": \"노트북 90% 할인\",\n" +
						"  \"startTime\": \"2025-05-01T00:00:00\",\n" +
						"  \"endTime\": \"2025-05-01T12:00:00\",\n" +
						"  \"discountRate\": 90,\n" +
						"  \"items\": [\n" +
						"    {\"itemId\": 1, \"quantity\": 5},\n" +
						"    {\"itemId\": 2, \"quantity\": 3},\n" +
						"    {\"itemId\": 3, \"quantity\": 10}\n" +
						"  ]\n" +
						"}"
				)
			)
		),
		responses = {
			@ApiResponse(
				responseCode = "200",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = TimeDealCreateResponse.class),
					examples = @ExampleObject(
						name = "타임딜 등록 응답 예시",
						value = "{\n" +
							"  \"timeDealId\": 12345,\n" +
							"  \"title\": \"노트북 90% 할인\",\n" +
							"  \"startTime\": \"2025-05-01T00:00:00\",\n" +
							"  \"endTime\": \"2025-05-01T12:00:00\",\n" +
							"  \"discountRate\": 90,\n" +
							"  \"items\": [\n" +
							"    {\"itemId\": 1, \"quantity\": 5},\n" +
							"    {\"itemId\": 2, \"quantity\": 3},\n" +
							"    {\"itemId\": 3, \"quantity\": 10}\n" +
							"  ]\n" +
							"}"
					)
				)
			)
		}
	)
	@PostMapping("/api/admin/time-deal")
	public BaseResponse<TimeDealCreateResponse> createTimeDeal(@RequestBody Map<String, Object> request) {
		TimeDealCreateResponse response = new TimeDealCreateResponse(
			12345L,
			"노트북 90% 할인",
			"2025-05-01T00:00:00",
			"2025-05-01T12:00:00",
			90,
			List.of(
				new TimeDealCreateItem(1L, 5),
				new TimeDealCreateItem(2L, 3),
				new TimeDealCreateItem(3L, 10)
			)
		);
		return BaseResponse.success(response);
	}

	@PostMapping("/api/admin/time-deal/modal")
	@Operation(
		summary = "타임딜 생성 모달 상품 조회",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = "[1, 2, 3]"
				)
			)
		),
		responses = {
			@ApiResponse(
				responseCode = "200",
				content = @Content(
					mediaType = "application/json",
					array = @ArraySchema(
						schema = @Schema(implementation = TimeDealModalItem.class)
					),
					examples = @ExampleObject(
						value = "[\n" +
							"  {\"itemId\": 1, \"itemName\": \"맥북 프로 16인치\", \"originalPrice\": 2990000},\n" +
							"  {\"itemId\": 2, \"itemName\": \"갤럭시 북3\", \"originalPrice\": 1890000},\n" +
							"  {\"itemId\": 3, \"itemName\": \"아이패드 에어 5세대\", \"originalPrice\": 929000}\n" +
							"]"
					)
				)
			)
		}
	)
	public BaseResponse<List<TimeDealModalItem>> getTimeDealModalItems(@RequestBody List<Long> itemIds) {
		List<TimeDealModalItem> result = List.of(
			new TimeDealModalItem(1L, "맥북 프로 16인치", 2990000),
			new TimeDealModalItem(2L, "갤럭시 북3", 1890000),
			new TimeDealModalItem(3L, "아이패드 에어 5세대", 929000)
		);
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
	@Operation(
		summary = "(관리자 페이지)타임딜 목록 조회",
		responses = {
			@ApiResponse(
				responseCode = "200",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = "[{ \"timeDealId\": 1, \"timeDealName\": \"노트북 90% 할인\", \"startTime\": \"2025-05-01T00:00:00\", \"endTime\": \"2025-05-01T12:00:00\", \"status\": \"ONGOING\", \"discountRate\": 90, \"items\": [{\"itemId\": 1, \"itemName\": \"맥북 프로\", \"quantity\": 5, \"originalPrice\": 1000000, \"finalPrice\": 100000}]}]"
					)
				)
			)
		}
	)
	public BaseResponse<List<Map<String, Object>>> searchTimeDeals() {
		List<Map<String, Object>> content = List.of(
			Map.of(
				"timeDealId", 1L,
				"timeDealName", "노트북 90% 할인",
				"startTime", "2025-05-01T00:00:00",
				"endTime", "2025-05-01T12:00:00",
				"status", "ONGOING",
				"discountRate", 90,
				"items", List.of(
					Map.of(
						"itemId", 1L,
						"itemName", "맥북 프로",
						"quantity", 5,
						"originalPrice", 1000000,
						"finalPrice", 100000
					),
					Map.of(
						"itemId", 2L,
						"itemName", "삼성 노트북",
						"quantity", 3,
						"originalPrice", 800000,
						"finalPrice", 80000
					)
				)
			),
			Map.of(
				"timeDealId", 2L,
				"timeDealName", "태블릿 70% 할인",
				"startTime", "2025-06-01T08:00:00",
				"endTime", "2025-06-01T18:00:00",
				"status", "UPCOMING",
				"discountRate", 70,
				"items", List.of(
					Map.of(
						"itemId", 3L,
						"itemName", "아이패드 프로",
						"quantity", 7,
						"originalPrice", 1200000,
						"finalPrice", 360000
					)
				)
			)
		);
		return BaseResponse.success(content);
	}
}
