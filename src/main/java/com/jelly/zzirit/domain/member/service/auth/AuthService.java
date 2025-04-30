package com.jelly.zzirit.domain.member.service.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.member.dto.request.SignupDTO;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.mapper.MemberMapper;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.domain.member.util.PasswordManager;
import com.jelly.zzirit.global.redis.RedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final MemberMapper memberMapper;
	private final PasswordManager passwordManager;
	private final RedisService redisService;
	private final BCryptPasswordEncoder passwordEncoder;

	private static final String REDIS_KEY_PREFIX = "emailAuth:";
	private static final String REDIS_KEY_SUFFIX = ":verified";

	@Transactional
	public void signup(SignupDTO signupDTO) {
		SignupValidationStatus.validateAll(signupDTO, memberRepository, redisService, passwordManager);
		createUser(signupDTO);
	}

	private void createUser(SignupDTO signupDTO) {
		Member member = memberMapper.ofSignupDTO(signupDTO);
		member.encodePassword(passwordEncoder);
		memberRepository.save(member);
		redisService.deleteData(REDIS_KEY_PREFIX + signupDTO.getMemberEmail() + REDIS_KEY_SUFFIX);
	}
}