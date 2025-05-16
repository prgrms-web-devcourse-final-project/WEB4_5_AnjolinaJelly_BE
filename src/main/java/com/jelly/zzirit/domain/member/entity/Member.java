package com.jelly.zzirit.domain.member.entity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.entity.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTime {

	@Column(name = "member_name", nullable = false, length = 50)
	private String memberName;

	@Column(name = "member_email", nullable = false, unique = true, length = 100)
	private String memberEmail;

	@Column(name = "member_password", nullable = false, length = 60)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "member_role", nullable = false, length = 10)
	@Builder.Default
	private Role role = Role.ROLE_USER;

	@Column(name = "member_address")
	private String memberAddress;

	@Column(name = "member_address_detail")
	private String memberAddressDetail;

	public void encodePassword(BCryptPasswordEncoder passwordEncoder) {
		this.password = passwordEncoder.encode(this.password);
	}

	public void updateAddress(String address, String addressDetail) {
		this.memberAddress = address;
		this.memberAddressDetail = addressDetail;
	}
}