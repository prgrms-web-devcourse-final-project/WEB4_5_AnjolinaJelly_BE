package com.jelly.zzirit.global.dataInit;

import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class TestAccountInitializer implements ApplicationRunner {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final StringRedisTemplate redisTemplate;

	@Override
	public void run(ApplicationArguments args) {
		initTestAccounts();
		clearRedis();
	}

	private void initTestAccounts() {

		if (memberRepository.findByMemberEmail("test001@example.com").isPresent()) {
			return;
		}

		String rawPassword = "test1234";
		for (int i = 1; i <= 100; i++) {
			String email = String.format("test%03d@example.com", i);
			String formatted = String.format("%03d", i);

			Member testUser = Member.builder()
				.memberEmail(email)
				.memberName("test" + formatted)
				.memberAddress("서울시 강남구 테스트로 " + i + "길")
				.memberAddressDetail("테스트빌딩 " + i + "층")
				.password(passwordEncoder.encode(rawPassword))
				.role(Role.ROLE_USER)
				.build();

			memberRepository.save(testUser);
		}
	}

	private void clearRedis() {
		Set<String> keys = redisTemplate.keys("emailAuth:*");
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}
}
