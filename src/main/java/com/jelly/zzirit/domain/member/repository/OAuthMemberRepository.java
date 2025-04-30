package com.jelly.zzirit.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.OAuthMember;
import com.jelly.zzirit.domain.member.entity.authenum.ProviderInfo;

public interface OAuthMemberRepository extends JpaRepository<OAuthMember, Long> {

	boolean existsByMemberAndProvider(Member member, ProviderInfo provider);
	// 특정 User 가 특정 소셜 Provider 계정을 가지고 있는지 확인

	@Query("SELECT o FROM OAuthMember o WHERE o.member.memberEmail = :email AND o.provider = :provider")
	Optional<OAuthMember> findByUserEmailAndProvider(@Param("email") String email, @Param("provider") ProviderInfo provider);
}