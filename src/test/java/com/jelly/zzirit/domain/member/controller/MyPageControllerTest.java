package com.jelly.zzirit.domain.member.controller;

import com.jelly.zzirit.global.support.TestMemberConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MyPageControllerIntegrationTest extends TestMemberConfig {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void 내정보조회_인증된_쿠키로_정상응답() throws Exception {
		mockMvc.perform(get("/api/user/mypage/info")
				.cookie(getAccessTokenCookie()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.result.memberName").value("테스트유저"));
	}

	@Test
	void 주소수정_정상요청시_200응답() throws Exception {
		String body = """
        {
            "memberAddress": "서울특별시 중구",
            "memberAddressDetail": "303호"
        }
        """;

		mockMvc.perform(patch("/api/user/mypage/address")
				.cookie(getAccessTokenCookie())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}
}