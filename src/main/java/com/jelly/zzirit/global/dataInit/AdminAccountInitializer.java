package com.jelly.zzirit.global.dataInit;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        String adminEmail = "admin@example.com";

        // 이미 존재하는지 확인
        if (memberRepository.findByMemberEmail(adminEmail).isPresent()) {
            return; // 중복 생성 방지
        }

        // 관리자 계정 생성
        Member admin = Member.builder()
                .memberEmail(adminEmail)
                .memberName("김채은매니절")
                .memberAddress("프랑스 쁘띠 궁전")
                .memberAddressDetail("펜트하우스")
                .password(passwordEncoder.encode("admin1234")) // 비밀번호는 반드시 인코딩
                .role(Role.ROLE_ADMIN)
                .build();

        memberRepository.save(admin);
    }
}
