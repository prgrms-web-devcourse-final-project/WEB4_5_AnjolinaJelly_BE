package com.jelly.zzirit.global.redis;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class TestContainerConfig {

	private static final String REDIS_IMAGE = "redis:7.0.8-alpine";
	private static final int REDIS_PORT = 6379;
	private static final String RABBIT_IMAGE = "rabbitmq:3.11-management";
	private static final int RABBIT_PORT = 5672;

	protected static final GenericContainer<?> REDIS_CONTAINER =
		new GenericContainer<>(REDIS_IMAGE)
			.withExposedPorts(REDIS_PORT)
			.withReuse(true);

	protected static final RabbitMQContainer RABBIT_CONTAINER =
		new RabbitMQContainer(RABBIT_IMAGE)
			.withExposedPorts(RABBIT_PORT)
			.withReuse(true);

	static {
		REDIS_CONTAINER.start();
		RABBIT_CONTAINER.start();
	}

	@DynamicPropertySource
	static void registerContainerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
		registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());

		registry.add("spring.rabbitmq.host", RABBIT_CONTAINER::getHost);
		registry.add("spring.rabbitmq.port", () -> RABBIT_CONTAINER.getMappedPort(RABBIT_PORT));
		registry.add("spring.rabbitmq.username", RABBIT_CONTAINER::getAdminUsername);
		registry.add("spring.rabbitmq.password", RABBIT_CONTAINER::getAdminPassword);
	}
}