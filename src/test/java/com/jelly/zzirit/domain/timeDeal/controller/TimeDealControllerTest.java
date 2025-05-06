package com.jelly.zzirit.domain.timeDeal.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.item.controller.TimeDealController;
import com.jelly.zzirit.domain.item.service.TimeDealService;
import com.jelly.zzirit.domain.timeDeal.dto.TimeDealModalItem;

@WebMvcTest(TimeDealController.class)
@AutoConfigureMockMvc(addFilters = false)
class TimeDealControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TimeDealService timeDealService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("타임딜 모달 상품 정보 조회 - 성공")
	void getTimeDealModalItems_success() throws Exception {
		// given
		List<Long> itemIds = List.of(1L, 2L);
		List<TimeDealModalItem> responseItems = List.of(
			new TimeDealModalItem(1L, "레노버 노트북 모델1", 1937000),
			new TimeDealModalItem(2L, "갤럭시 북", 1890000)
		);

		given(timeDealService.getModalItems(itemIds)).willReturn(responseItems);

		// when & then
		mockMvc.perform(post("/api/admin/time-deal/modal")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(itemIds)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result[0].itemId").value(1L))
			.andExpect(jsonPath("$.result[0].itemName").value("레노버 노트북 모델1"))
			.andExpect(jsonPath("$.result[0].originalPrice").value(1937000));
	}
}
