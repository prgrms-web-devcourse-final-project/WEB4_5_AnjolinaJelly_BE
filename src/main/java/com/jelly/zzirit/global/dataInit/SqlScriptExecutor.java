package com.jelly.zzirit.global.dataInit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqlScriptExecutor {

	private final ResourceLoader resourceLoader;

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void executeSqlFile(String path) {
		try {
			Resource resource = resourceLoader.getResource(path);
			String sqlContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			List<String> statements = List.of(sqlContent.split(";"));

			for (String statement : statements) {
				String trimmed = statement.trim();
				if (!trimmed.isEmpty()) {
					entityManager.createNativeQuery(trimmed).executeUpdate();
				}
			}

		} catch (Exception e) {
			log.error("SQL 파일 실행 실패");
		}
	}
}