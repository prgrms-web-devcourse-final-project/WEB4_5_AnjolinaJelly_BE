package com.jelly.zzirit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.jelly.zzirit.global.redis.RedisRabbitTestContainerConfig;

@SpringBootTest
class ZziritApplicationTests extends RedisRabbitTestContainerConfig {

	@Test
	void contextLoads() {
	}

}