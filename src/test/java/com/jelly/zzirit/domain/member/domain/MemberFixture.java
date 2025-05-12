package com.jelly.zzirit.domain.member.domain;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;

import java.util.UUID;

public class MemberFixture {
    public static Member 일반_회원() {
        return Member.builder()
            .memberName("테스트 유저")
            .memberEmail(generateUniqueEmail())
            .password("1234abcd!")
            .role(Role.ROLE_USER)
            .memberAddress("서울특별시 강남구")
            .memberAddressDetail("101동 202호")
            .build();
    }

    private static String generateUniqueEmail() {
        return "mail-" + UUID.randomUUID() + "@test.com";
    }
}
