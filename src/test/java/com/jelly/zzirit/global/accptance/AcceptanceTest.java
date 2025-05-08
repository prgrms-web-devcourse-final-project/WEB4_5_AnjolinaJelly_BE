package com.jelly.zzirit.global.accptance;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.security.service.TokenService;
import com.jelly.zzirit.global.security.util.AuthConst;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AcceptanceTest  {

	@LocalServerPort
	int port;

	protected RequestSpecification spec;

	@Autowired
	protected MemberRepository memberRepository;

	@Autowired
	protected TokenService tokenService;

	@Autowired
	protected BCryptPasswordEncoder passwordEncoder;

	protected String testAccessToken;
	protected Long testMemberId;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	@BeforeEach
	void setUp(RestDocumentationContextProvider provider) {
		this.spec = new RequestSpecBuilder()
			.setPort(port)
			.addFilter(
				documentationConfiguration(provider)
					.operationPreprocessors()
					.withRequestDefaults(prettyPrint())
					.withResponseDefaults(prettyPrint())
			)
			.build();
	}

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
		this.testAccessToken = tokenService.generateAccessToken(member.getId(), member.getRole());
	}


	protected Cookie getAccessTokenCookie() {
		return new Cookie(AuthConst.TOKEN_TYPE_ACCESS, testAccessToken);
	}
}
