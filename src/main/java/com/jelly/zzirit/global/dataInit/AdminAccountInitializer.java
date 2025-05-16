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
public class AdminAccountInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        initAdminAccount();
        clearRedis();
    }

    private void initAdminAccount() {
        String adminEmail = "admin@example.com";

        if (memberRepository.findByMemberEmail(adminEmail).isPresent()) {
            return;
        }

        Member admin = Member.builder()
            .memberEmail(adminEmail)
            .memberName("김채은매니절")
            .memberAddress("프랑스 쁘띠 궁전")
            .memberAddressDetail("펜트하우스")
            .password(passwordEncoder.encode("admin1234"))
            .role(Role.ROLE_ADMIN)
            .build();

        memberRepository.save(admin);
    }

    private void clearRedis() {
        Set<String> keys = redisTemplate.keys("emailAuth:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}