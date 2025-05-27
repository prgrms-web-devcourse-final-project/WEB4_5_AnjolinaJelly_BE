package com.jelly.zzirit.global.dataInit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Profile({"dev", "prod"})
@Component
@RequiredArgsConstructor
public class TimeDealDummyDataRunner implements CommandLineRunner {

	private final TimeDealDummyDataGenerator generator;

	@Override
	public void run(String... args) throws Exception {
		generator.generateInitialData();
	}
}
