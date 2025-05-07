package com.jelly.zzirit.domain.item;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.item.dto.timeDeal.TimeDealModalItem;
import com.jelly.zzirit.domain.item.dto.timeDeal.request.TimeDealCreateItemDetail;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.timeDeal.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.global.security.util.JwtUtil;

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

	@Test
	void 타임딜_등록_성공() throws Exception {
		// given
		Long userId = 1L;
		Role role = Role.ROLE_ADMIN;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600); // 1시간 유효한 access token

		var request = new TimeDealCreateRequest(
			"노트북 타임딜",
			LocalDateTime.of(2025, 5, 10, 10, 0),
			LocalDateTime.of(2025, 5, 11, 10, 0),
			10,
			List.of(
				new TimeDealCreateItemDetail(1L, 5),
				new TimeDealCreateItemDetail(2L, 5),
				new TimeDealCreateItemDetail(3L, 5),
				new TimeDealCreateItemDetail(4L, 5)
			)
		);

		// When & Then
		mockMvc.perform(
				post("/api/admin/time-deal")
					.cookie(new Cookie("access", accessToken))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.title").value("노트북 타임딜"))
			.andExpect(jsonPath("$.result.items").isArray())
			.andExpect(jsonPath("$.result.items.length()").value(4));
	}

	@Test
	@DisplayName("타임딜 모달 상품 정보 조회 - 성공")
	void getTimeDealModalItems_success() throws Exception {
		// given
		Long userId = 1L;
		Role role = Role.ROLE_ADMIN;
		String accessToken = jwtUtil.createJwt("access", userId, role, 3600); // 1시간 유효한 access token

		List<Long> itemIds = List.of(1L, 2L);
		List<TimeDealModalItem> responseItems = List.of(
			new TimeDealModalItem(1L, "레노버 노트북 ThinkPad X1 Carbon", 1650000),
			new TimeDealModalItem(2L, "소니 노트북 VAIO Pro", 1450000)
		);
		// when & then
		mockMvc.perform(post("/api/admin/time-deal/modal")
				.cookie(new Cookie("access", accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(itemIds)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result[0].itemId").value(1L))
			.andExpect(jsonPath("$.result[0].itemName").value("레노버 노트북 ThinkPad X1 Carbon"))
			.andExpect(jsonPath("$.result[0].originalPrice").value(1650000));
	}
}