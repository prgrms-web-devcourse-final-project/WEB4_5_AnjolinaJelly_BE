package com.jelly.zzirit.global.security.util;

import com.jelly.zzirit.global.config.AppConfig;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {

	/**
	 * 쿠키 생성 메서드
	 *
	 * @param key    쿠키 이름
	 * @param value  쿠키 값
	 * @param maxAge 쿠키 유효 시간 (초 단위)
	 * @return 생성된 Cookie 객체
	 */
	public static Cookie createCookie(String key, String value, int maxAge) {
		Cookie cookie = new Cookie(key, value);
		cookie.setDomain(AppConfig.getSiteDomain());
		cookie.setMaxAge(maxAge);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setAttribute("SameSite", "None");
		return cookie;
	}

	/**
	 * 쿠키 삭제용 빈 쿠키 생성 메서드
	 *
	 * @param key 삭제할 쿠키 이름
	 * @return 삭제용 Cookie 객체 (maxAge = 0)
	 */
	public static Cookie deleteCookie(String key) {
		Cookie cookie = new Cookie(key, "");
		cookie.setMaxAge(0);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setAttribute("SameSite", "None");
		return cookie;
	}

	/**
	 * 요청에서 특정 쿠키 값 조회
	 *
	 * @param request    HttpServletRequest
	 * @param cookieName 조회할 쿠키 이름
	 * @return 쿠키 값 (없으면 null)
	 */
	public static String getCookieValue(HttpServletRequest request, String cookieName) {
		if (request == null || request.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : request.getCookies()) {
			if (cookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}