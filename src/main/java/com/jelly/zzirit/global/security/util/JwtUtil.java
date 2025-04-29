package com.jelly.zzirit.global.security.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.member.entity.authenum.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final SecretKey secretKey;

	public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public Claims getPayload(String token) throws JwtException {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public String getCategory(String token) {
		return extractClaim(token, AuthConst.TOKEN_TYPE_CATEGORY, String.class);
	}

	public Long getUserId(String token) {
		return extractClaim(token, AuthConst.TOKEN_USER_ID, Number.class).longValue();
	}

	public Role getRole(String token) {
		return Role.valueOf(extractClaim(token, AuthConst.TOKEN_ROLE, String.class));
	}

	public boolean isExpired(String token) {
		try {
			return getPayload(token).getExpiration().before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		} catch (JwtException e) {
			return false;
		}
	}

	public long getExpiration(String token) {
		return Math.max(getPayload(token).getExpiration().getTime() - System.currentTimeMillis(), 0);
	}

	private <T> T extractClaim(String token, String claimKey, Class<T> clazz) {
		return getPayload(token).get(claimKey, clazz);
	}

	public String createJwt(String category, Long userId, Role role, long expiredMs) {
		return createJwt(Map.of(
			AuthConst.TOKEN_TYPE_CATEGORY, category,
			AuthConst.TOKEN_USER_ID, userId,
			AuthConst.TOKEN_ROLE, role.name(),
			"jti", UUID.randomUUID().toString()
		), expiredMs);
	}

	public String createJwt(Map<String, Object> claims, long expiredMs) {
		Date now = new Date();
		return Jwts.builder()
			.claims(claims)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + expiredMs))
			.signWith(secretKey)
			.compact();
	}
}