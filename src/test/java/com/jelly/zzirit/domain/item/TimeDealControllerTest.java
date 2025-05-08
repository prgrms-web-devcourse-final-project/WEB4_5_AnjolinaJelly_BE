package com.jelly.zzirit.domain.item;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.dto.response.timeDeal.TimeDealModalCreateResponse;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.testutil.TimeDealTestHelper;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
public class TimeDealControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	TimeDealTestHelper timeDealTestHelper;

	@Autowired
	private TimeDealItemRepository timeDealItemRepository;

	@Autowired
	private TimeDealRepository timeDealRepository;

	@Autowired
	private ItemStockRepository itemStockRepository;

	@BeforeEach
	void cleanUp() {
		timeDealItemRepository.deleteAllInBatch();
		timeDealRepository.deleteAllInBatch();
		itemStockRepository.deleteAllInBatch();
	}   // 재고 삭제

	@Test
	void 타임딜_등록_성공() throws Exception {
		// Given
		Long userId = 1L;
		Role role = Role.ROLE_ADMIN;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600); // 1시간 유효한 access token

		var request = new TimeDealCreateRequest(
			"노트북 타임딜",
			LocalDateTime.of(2025, 5, 10, 10, 0),
			LocalDateTime.of(2025, 5, 11, 10, 0),
			10,
			List.of(
				new TimeDealCreateRequest.TimeDealCreateItemDetail(1L, 5),
				new TimeDealCreateRequest.TimeDealCreateItemDetail(2L, 5),
				new TimeDealCreateRequest.TimeDealCreateItemDetail(3L, 5),
				new TimeDealCreateRequest.TimeDealCreateItemDetail(4L, 5)
			)
		);

		// When & Then
		mockMvc.perform(
				post("/api/admin/time-deals")
					.cookie(new Cookie("access", accessToken))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.timeDealName").value("노트북 타임딜"))
			.andExpect(jsonPath("$.result.items").isArray())
			.andExpect(jsonPath("$.result.items.length()").value(4));
	}

	@Test
	@DisplayName("현재 진행중인 타임딜 조회 성공")
	void 현재_진행중인_타임딜_조회_성공() throws Exception {
		// Given
		Long userId = 1L;
		Role role = Role.ROLE_ADMIN;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600); // 1시간 유효한 access token

		// 테스트용 타임딜 데이터 생성
		TimeDealCreateRequest request = new TimeDealCreateRequest(
			"테스트 타임딜",
			LocalDateTime.now().plusHours(1),
			LocalDateTime.now().plusHours(2),
			20,
			List.of(
				new TimeDealCreateRequest.TimeDealCreateItemDetail(1L, 3),
				new TimeDealCreateRequest.TimeDealCreateItemDetail(2L, 3)
			)
		);

		// ONGOING 상태의 타임딜 생성
		timeDealTestHelper.createOngoingTimeDeal(request);

		// When & Then
		mockMvc.perform(get("/api/time-deals/now")
				.cookie(new Cookie("access", accessToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.timeDealId").isNumber())
			.andExpect(jsonPath("$.result.timeDealName").isString())
			.andExpect(jsonPath("$.result.items").isArray());
	}

	@Test
	@DisplayName("타임딜 모달 상품 정보 조회 성공")
	void 타임딜_모달_상품_정보_조회_성공() throws Exception {
		// Given
		Long userId = 1L;
		Role role = Role.ROLE_ADMIN;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600); // 1시간 유효한 access token

		List<Long> itemIds = List.of(1L, 2L);
		List<TimeDealModalCreateResponse> responseItems = List.of(
			new TimeDealModalCreateResponse(1L, "레노버 노트북 ThinkPad X1 Carbon", 1650000),
			new TimeDealModalCreateResponse(2L, "소니 노트북 VAIO Pro", 1450000)
		);
		// When & Then
		mockMvc.perform(post("/api/admin/time-deals/modal")
				.cookie(new Cookie("access", accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(itemIds)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result[0].itemId").value(1L))
			.andExpect(jsonPath("$.result[0].itemName").value("레노버 노트북 ThinkPad X1 Carbon"))
			.andExpect(jsonPath("$.result[0].originalPrice").value(1650000));
	}

	@Test
	@DisplayName("타임딜 목록 검색 및 필터링 성공")
	void 타임딜_목록_검색_및_필터링_성공() throws Exception {
		Long userId = 1L;
		Role role = Role.ROLE_ADMIN;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600); // 1시간 유효한 access token

		mockMvc.perform(get("/api/admin/time-deals/search")
				.cookie(new Cookie("access", accessToken))
				.param("timeDealName", "노트북")
				.param("status", "ONGOING")
				.param("page", "0")
				.param("size", "10")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.content").isArray())
			.andExpect(jsonPath("$.result.content.length()").isNumber());
	}
}
