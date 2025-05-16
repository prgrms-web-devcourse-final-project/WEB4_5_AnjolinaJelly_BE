package com.jelly.zzirit.domain.item.contorller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.support.RabbitTestMemberConfig;
import com.jelly.zzirit.testutil.TimeDealTestHelper;

@SpringBootTest
@AutoConfigureMockMvc
@Disabled
public class TimeDealControllerTest extends RabbitTestMemberConfig {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	TimeDealTestHelper timeDealTestHelper;

	@Test
	void 타임딜_등록_성공() throws Exception {
		// Given
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
					.cookie(getAccessTokenCookie())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.timeDealName").value("노트북 타임딜"))
			.andExpect(jsonPath("$.result.items").isArray())
			.andExpect(jsonPath("$.result.items.length()").value(4));
	}

	@Test
	void 현재_진행중인_타임딜_조회_성공() throws Exception {
		// Given
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
				.cookie(getAccessTokenCookie()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.timeDealId").isNumber())
			.andExpect(jsonPath("$.result.timeDealName").isString())
			.andExpect(jsonPath("$.result.items").isArray());
	}

	@Test
	void 타임딜_목록_검색_및_필터링_성공() throws Exception {
		mockMvc.perform(get("/api/admin/time-deals/search")
				.cookie(getAccessTokenCookie())
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
