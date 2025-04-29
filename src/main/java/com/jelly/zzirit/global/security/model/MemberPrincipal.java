package com.jelly.zzirit.global.security.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.jelly.zzirit.domain.member.entity.authenum.Role;

import lombok.Getter;

@Getter
public class MemberPrincipal implements UserDetails, OAuth2User {

	private final Long memberId;
	private final String memberPassword;
	private final Role role;
	private final Collection<? extends GrantedAuthority> authorities;

	public MemberPrincipal(Long memberId, String memberPassword, Role role) {
		this.memberId = memberId;
		this.memberPassword = memberPassword;
		this.role = role;
		this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));
	}

	public MemberPrincipal(Long memberId, Role role) {
		this.memberId = memberId;
		this.memberPassword = null;
		this.role = role;
		this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));
	}

	@Override
	public String getUsername() {
		return String.valueOf(memberId);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return memberPassword;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public String getName() {
		return String.valueOf(memberId);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}