package com.jelly.zzirit.global.rabbitmq;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class RabbitMQTestContainerConfig {

	private static final String RABBIT_IMAGE = "rabbitmq:3.11-management";
	private static final int RABBIT_PORT = 5672;

	protected static final RabbitMQContainer RABBIT_CONTAINER =
		new RabbitMQContainer(RABBIT_IMAGE)
			.withExposedPorts(RABBIT_PORT)
			.withReuse(true);

	static {
		RABBIT_CONTAINER.start();
	}

	@DynamicPropertySource
	static void registerRabbitProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host", RABBIT_CONTAINER::getHost);
		registry.add("spring.rabbitmq.port", () -> RABBIT_CONTAINER.getMappedPort(RABBIT_PORT));
		registry.add("spring.rabbitmq.username", RABBIT_CONTAINER::getAdminUsername);
		registry.add("spring.rabbitmq.password", RABBIT_CONTAINER::getAdminPassword);
	}
}