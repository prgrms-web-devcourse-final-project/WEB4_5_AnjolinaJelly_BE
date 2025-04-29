package com.jelly.zzirit.global.security.service;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidTokenException;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.security.util.RedisRefreshTokenUtil;

enum RefreshTokenValidationStatus {

	TOKEN_NULL {
		@Override
		public void validate(String refreshToken, JwtUtil jwtUtil, RedisRefreshTokenUtil redisRefreshTokenUtil) {
			if (refreshToken == null) {
				throw new InvalidTokenException(BaseResponseStatus.REFRESH_TOKEN_NULL);
			}
		}
	},

	TOKEN_EXPIRED {
		@Override
		public void validate(String refreshToken, JwtUtil jwtUtil, RedisRefreshTokenUtil redisRefreshTokenUtil) {
			if (jwtUtil.isExpired(refreshToken)) {
				throw new InvalidTokenException(BaseResponseStatus.REFRESH_TOKEN_EXPIRED);
			}
		}
	},

	TOKEN_CATEGORY_INVALID {
		@Override
		public void validate(String refreshToken, JwtUtil jwtUtil, RedisRefreshTokenUtil redisRefreshTokenUtil) {
			if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
				throw new InvalidTokenException(BaseResponseStatus.REFRESH_TOKEN_INVALID);
			}
		}
	},

	TOKEN_NOT_FOUND {
		@Override
		public void validate(String refreshToken, JwtUtil jwtUtil, RedisRefreshTokenUtil redisRefreshTokenUtil) {
			Long userId = jwtUtil.getUserId(refreshToken);
			String storedRefresh = redisRefreshTokenUtil.getRefreshToken(userId);
			if (storedRefresh == null || !storedRefresh.equals(refreshToken)) {
				throw new InvalidTokenException(BaseResponseStatus.REFRESH_TOKEN_NOT_FOUND);
			}
		}
	};

	abstract void validate(String refreshToken, JwtUtil jwtUtil, RedisRefreshTokenUtil redisRefreshTokenUtil);

	public static void validateToken(String refreshToken, JwtUtil jwtUtil, RedisRefreshTokenUtil redisRefreshTokenUtil) {
		for (RefreshTokenValidationStatus status : RefreshTokenValidationStatus.values()) {
			status.validate(refreshToken, jwtUtil, redisRefreshTokenUtil);
		}
	}
}