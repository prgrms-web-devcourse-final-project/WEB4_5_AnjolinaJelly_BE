package com.jelly.zzirit.global.support;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.redis.TestContainerConfig;
import com.jelly.zzirit.global.security.service.TokenService;
import com.jelly.zzirit.global.security.util.AuthConst;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Cookie;
import io.restassured.specification.RequestSpecification;

@AutoConfigureRestDocs
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Sql(scripts = {"classpath:truncate.sql"}, executionPhase = AFTER_TEST_METHOD)
public abstract class AcceptanceRabbitTest extends TestContainerConfig {

	@LocalServerPort
	int port;

	protected RequestSpecification spec;

	@Autowired
	protected TokenService tokenService;

	@BeforeEach
	void setUp(RestDocumentationContextProvider provider) {
		this.spec =  new RequestSpecBuilder()
			.setPort(port)
			.addFilter(
				documentationConfiguration(provider)
					.operationPreprocessors()
					.withRequestDefaults(prettyPrint())
					.withResponseDefaults(prettyPrint())
			)
			.build();
	}

	public Cookie getCookie() {
		return getCookie(1L);
	}

	public Cookie getCookie(Long userId) {
		return new Cookie.Builder(
			AuthConst.TOKEN_TYPE_ACCESS,
			tokenService.generateAccessToken(userId, Role.ROLE_USER)
		).build();
	}
}