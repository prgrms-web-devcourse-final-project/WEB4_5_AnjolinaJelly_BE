package com.jelly.zzirit.global.support;

import static io.restassured.RestAssured.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.restassured.specification.RequestSpecification;

@AutoConfigureRestDocs
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public abstract class RestDocsSupport {

	@LocalServerPort
	int port;

	protected RequestSpecification spec;

	@BeforeEach
	void setUp(RestDocumentationContextProvider provider) {
		this.spec = given()
			.port(port)
			.filter(documentationConfiguration(provider));
	}
}
