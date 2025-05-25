package com.jelly.zzirit.global.dataInit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Profile({"dev", "prod"})
@Component
@RequiredArgsConstructor
public class TimeDealDummyDataRunner implements CommandLineRunner {

    private final TimeDealDummyDataGenerator generator;

    @Override
    public void run(String... args) throws Exception {
        generator.generateInitialData(20000, 1);
    }
}
