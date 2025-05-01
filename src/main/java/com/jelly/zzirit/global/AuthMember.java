package com.jelly.zzirit.global;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;
import com.jelly.zzirit.global.security.model.MemberPrincipal;

@Component
public class AuthMember {

	public static Member getAuthUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
			throw new InvalidUserException(BaseResponseStatus.AUTH_CHECK_FAILED);
		}

		Object principal = authentication.getPrincipal();
		MemberPrincipal user = (MemberPrincipal) principal;

		return Member.builder()
			.id(Long.parseLong(user.getUsername()))
			.role(Role.valueOf(user.getRole().name()))
			.build();
	}

	public static Role getMemberRole() {
		return getAuthUser().getRole();
	}
}