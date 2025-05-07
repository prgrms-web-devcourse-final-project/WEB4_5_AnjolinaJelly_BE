package com.jelly.zzirit.global.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.security.service.TokenService;
import com.jelly.zzirit.global.security.util.AuthConst;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@ActiveProfiles("test")
public abstract class TestMemberConfig {

	@Autowired
	protected MemberRepository memberRepository;

	@Autowired
	protected TokenService tokenService;

	@Autowired
	protected BCryptPasswordEncoder passwordEncoder;

	protected String testAccessToken;
	protected Long testMemberId;

	@PostConstruct
	public void initTestMember() {
		String email = "test@example.com";

		Member member = memberRepository.findByMemberEmail(email)
			.orElseGet(() -> {
				Member newMember = Member.builder()
					.memberEmail(email)
					.memberName("테스트유저")
					.password(passwordEncoder.encode("test1234!"))
					.role(Role.ROLE_USER)
					.memberAddress("서울")
					.memberAddressDetail("101동")
					.build();
				return memberRepository.save(newMember);
			});

		this.testMemberId = member.getId();
		this.testAccessToken = tokenService.generateAccessToken(member.getId(), member.getRole());	}


	protected Cookie getAccessTokenCookie() {
		return new Cookie(AuthConst.TOKEN_TYPE_ACCESS, testAccessToken);
	} // 테스트에서 사용할 AccessToken 쿠키를 반환
}