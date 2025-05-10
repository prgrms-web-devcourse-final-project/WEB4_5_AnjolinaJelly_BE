package com.jelly.zzirit.global.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.redis.RedisTestContainerConfig;
import com.jelly.zzirit.global.security.service.TokenService;
import com.jelly.zzirit.global.security.util.AuthConst;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;

@SpringBootTest
public abstract class TestMemberConfig extends RedisTestContainerConfig {

	@Autowired
	protected MemberRepository memberRepository;

	@Autowired
	protected TokenService tokenService;

	@Autowired
	protected BCryptPasswordEncoder passwordEncoder;

	protected String testAccessToken;
	protected Long testMemberId;

	protected String adminAccessToken;
	protected Long adminMemberId;


	@PostConstruct
	public void initTestMember(){
		initTestUser();
		initAdminUser();
	}

	private void initTestUser() {
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
		this.testAccessToken = tokenService.generateAccessToken(member.getId(), member.getRole());
	}

	private void initAdminUser() {
		String adminEmail = "admin@example.com";

		Member admin = memberRepository.findByMemberEmail(adminEmail)
				.orElseGet(() -> {
					Member newAdmin = Member.builder()
							.memberEmail(adminEmail)
							.memberName("테스트관리자")
							.password(passwordEncoder.encode("admin1234!"))
							.role(Role.ROLE_ADMIN)
							.memberAddress("서울")
							.memberAddressDetail("202동")
							.build();
					return memberRepository.save(newAdmin);
				});

		this.adminMemberId = admin.getId();
		this.adminAccessToken = tokenService.generateAccessToken(admin.getId(), admin.getRole());
	}


	protected Cookie getAccessTokenCookie() {
		return new Cookie(AuthConst.TOKEN_TYPE_ACCESS, testAccessToken);
	} // 테스트에서 사용할 AccessToken 쿠키를 반환

	protected Cookie getAdminAccessTokenCookie() {
		return new Cookie(AuthConst.TOKEN_TYPE_ACCESS, adminAccessToken);
	} // 테스트에서 사용할 AccessToken 쿠키를 반환
}