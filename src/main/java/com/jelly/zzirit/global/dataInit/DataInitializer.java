package com.jelly.zzirit.global.dataInit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jelly.zzirit.domain.item.repository.TypeRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

	private final TypeRepository typeRepository;
	private final SqlScriptExecutor sqlScriptExecutor;

	@Bean
	public CommandLineRunner importSqlIfEmpty() {
		return args -> {
			if (typeRepository.count() == 0) {
				sqlScriptExecutor.executeSqlFile("classpath:data.sql");
				System.out.println("✅ JPA 기반 SQL 파일 실행 완료");
			} else {
				System.out.println("ℹ️ 데이터가 이미 있어 SQL 삽입 생략됨");
			}
		};
	}
}
