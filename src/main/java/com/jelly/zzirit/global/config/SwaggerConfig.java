package com.jelly.zzirit.global.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI springShopOpenAPI() {
		Server server = new Server();
		server.setUrl("https://api.zzirit.shop");

		return new OpenAPI()
			.components(new Components().addSecuritySchemes("bearer", this.securityScheme()))
			.security(Collections.singletonList(this.securityRequirement()))
			.info(
				new Info().title("zzirit API")
					.description("찌릿 API 명세서")
					.version("v0.0.1")
			)
			.servers(List.of(server));
	}

	private SecurityScheme securityScheme() {
		return new SecurityScheme()
			.type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER).name("Authorization");
	}

	private SecurityRequirement securityRequirement() {
		return new SecurityRequirement().addList("bearer");
	}
}
