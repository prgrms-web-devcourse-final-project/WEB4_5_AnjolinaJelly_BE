package com.jelly.zzirit.domain.member.entity.authenum;

import lombok.Getter;

@Getter
public enum Role {
	ROLE_USER,
	ROLE_ADMIN,
	ROLE_GUEST;

	public String getKey() {
		return name().replace("ROLE_", "");
	}

	public String getAuthority() {
		return name();
	}
}