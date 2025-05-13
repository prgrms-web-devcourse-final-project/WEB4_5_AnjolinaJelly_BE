package com.jelly.zzirit.domain.member.service.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.jelly.zzirit.domain.member.dto.request.SignupRequest;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.mapper.MemberMapper;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.domain.member.util.PasswordManager;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;
import com.jelly.zzirit.global.redis.RedisService;

class CommandAuthServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private MemberMapper memberMapper;

	@Mock
	private PasswordManager passwordManager;

	@Mock
	private RedisService redisService;

	@Mock
	private BCryptPasswordEncoder passwordEncoder;

	@InjectMocks
	private CommandAuthService commandAuthService;

	private static final String TEST_EMAIL = "test@example.com";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void 회원가입_성공() {
		// given
		SignupRequest signupRequest = new SignupRequest("홍길동", TEST_EMAIL, "Password123!", "서울시 강남구", "101동 202호");

		when(redisService.getData("emailAuth:" + TEST_EMAIL + ":verified")).thenReturn("true");
		when(passwordManager.isInvalid(signupRequest.getMemberPassword())).thenReturn(false);
		when(memberRepository.findByMemberEmail(TEST_EMAIL)).thenReturn(Optional.empty());

		Member member = mock(Member.class);
		when(memberMapper.ofSignupDTO(signupRequest)).thenReturn(member);

		// when
		commandAuthService.signup(signupRequest);

		// then
		verify(memberMapper).ofSignupDTO(signupRequest);
		verify(member).encodePassword(passwordEncoder);
		verify(memberRepository).save(member);
		verify(redisService).deleteData("emailAuth:" + TEST_EMAIL + ":verified");
	}

	@Test
	void 회원가입_실패_이메일_미인증() {
		// given
		SignupRequest signupRequest = new SignupRequest("홍길동", TEST_EMAIL, "Password123!", "서울시 강남구", "101동 202호");

		when(redisService.getData("emailAuth:" + TEST_EMAIL + ":verified")).thenReturn("false");

		// when & then
		assertThatThrownBy(() -> commandAuthService.signup(signupRequest))
			.isInstanceOf(InvalidUserException.class)
			.hasMessageContaining("이메일 인증이 필요합니다");
	}

	@Test
	void 회원가입_실패_비밀번호_규칙_위반() {
		// given
		SignupRequest signupRequest = new SignupRequest("홍길동", TEST_EMAIL, "weak", "서울시 강남구", "101동 202호");

		when(redisService.getData("emailAuth:" + TEST_EMAIL + ":verified")).thenReturn("true");
		when(passwordManager.isInvalid(signupRequest.getMemberPassword())).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> commandAuthService.signup(signupRequest))
			.isInstanceOf(InvalidUserException.class)
			.hasMessageContaining("비밀번호는 8~15자 이내로 숫자와 소문자를 포함해야 합니다.");
	}

	@Test
	void 회원가입_실패_이메일_중복() {
		// given
		SignupRequest signupRequest = new SignupRequest("홍길동", TEST_EMAIL, "Password123!", "서울시 강남구", "101동 202호");

		when(redisService.getData("emailAuth:" + TEST_EMAIL + ":verified")).thenReturn("true");
		when(passwordManager.isInvalid(signupRequest.getMemberPassword())).thenReturn(false);
		when(memberRepository.findByMemberEmail(TEST_EMAIL)).thenReturn(Optional.of(mock(Member.class)));

		// when & then
		assertThatThrownBy(() -> commandAuthService.signup(signupRequest))
			.isInstanceOf(InvalidUserException.class)
			.hasMessageContaining("이미 가입된 이메일입니다. 다른 방법으로 로그인하세요.");
	}
}
