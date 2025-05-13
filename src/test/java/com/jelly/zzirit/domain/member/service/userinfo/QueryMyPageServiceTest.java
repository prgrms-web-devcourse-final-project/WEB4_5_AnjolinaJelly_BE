package com.jelly.zzirit.domain.member.service.userinfo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.member.dto.response.MyPageInfoResponse;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

@ExtendWith(MockitoExtension.class)
class QueryMyPageServiceTest {

	@InjectMocks
	private QueryMyPageService queryMyPageService;

	@Mock
	private MemberRepository memberRepository;

	private final Long mockMemberId = 1L;

	@Test
	void 정상조회() {
		// given
		Member member = Member.builder()
			.id(mockMemberId)
			.memberName("홍길동")
			.memberAddress("서울시 강남구")
			.memberAddressDetail("101동 1001호")
			.build();

		try (MockedStatic<AuthMember> mockAuth = Mockito.mockStatic(AuthMember.class)) {
			mockAuth.when(AuthMember::getMemberId).thenReturn(mockMemberId);
			when(memberRepository.findById(mockMemberId)).thenReturn(Optional.of(member));

			// when
			MyPageInfoResponse result = queryMyPageService.getMyPageInfo();

			// then
			assertEquals("홍길동", result.getMemberName());
			assertEquals("서울시 강남구", result.getMemberAddress());
			assertEquals("101동 1001호", result.getMemberAddressDetail());
		}
	}

	@Test
	void 사용자없으면_예외발생() {
		try (MockedStatic<AuthMember> mockAuth = Mockito.mockStatic(AuthMember.class)) {
			mockAuth.when(AuthMember::getMemberId).thenReturn(mockMemberId);
			when(memberRepository.findById(mockMemberId)).thenReturn(Optional.empty());

			// then
			assertThrows(InvalidUserException.class, () -> queryMyPageService.getMyPageInfo());
		}
	}
}