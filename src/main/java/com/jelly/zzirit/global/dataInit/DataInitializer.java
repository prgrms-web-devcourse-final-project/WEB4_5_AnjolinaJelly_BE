package com.jelly.zzirit.global.dataInit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jelly.zzirit.domain.item.repository.ItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

	private final ItemRepository itemRepository;
	private final SqlScriptExecutor sqlScriptExecutor;

	@Bean
	@Order(1)
	public CommandLineRunner importSqlIfEmpty() {
		return args -> {
			if (itemRepository.count() < 2) {
				sqlScriptExecutor.executeSqlFile("classpath:data.sql");
				log.info("JPA 기반 SQL 파일 실행 완료");
			} else {
				log.info("데이터가 이미 있어 SQL 삽입 생략됨");
			}
		};
	}
}