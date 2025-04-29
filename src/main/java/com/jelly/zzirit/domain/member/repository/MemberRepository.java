package com.jelly.zzirit.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jelly.zzirit.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	@Query("select m.password from Member m where m.id = :memberId")
	Optional<String> findPasswordById(@Param("memberId") Long memberId);

	Optional<Member> findByMemberEmail(String memberEmail);
}