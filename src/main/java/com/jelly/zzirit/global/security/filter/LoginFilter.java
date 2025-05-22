package com.jelly.zzirit.global.security.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;
import com.jelly.zzirit.global.exception.custom.InvalidRedisException;
import com.jelly.zzirit.global.security.model.MemberPrincipal;
import com.jelly.zzirit.global.security.service.TokenService;
import com.jelly.zzirit.global.security.util.AccountLoginRateUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

	private final ObjectMapper objectMapper;
	private final TokenService tokenService;
	private final AccountLoginRateUtil rateLimiter;

	public LoginFilter(AuthenticationManager authenticationManager,
		ObjectMapper objectMapper,
		TokenService tokenService,
		AccountLoginRateUtil rateLimiter) {
		super.setAuthenticationManager(authenticationManager);
		this.objectMapper = objectMapper;
		this.tokenService = tokenService;
		this.rateLimiter = rateLimiter;
		setFilterProcessesUrl("/api/auth/basic/login");
	}

	private String extractClientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
		try {
			Map<String, String> credentials = objectMapper.readValue(req.getInputStream(), new TypeReference<>() {});
			String email = credentials.get("username");
			String password = credentials.get("password");
			String ip = extractClientIp(req);

			rateLimiter.checkIpRateLimit(ip);
			rateLimiter.checkAccountLock(email);

			log.info("로그인 시도: email={}, ip={}", email, ip);

			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
			return getAuthenticationManager().authenticate(authToken);

		} catch (InvalidAuthenticationException | InvalidRedisException e) {
			SecurityContextHolder.clearContext();
			req.setAttribute("exception", e);
			throw new BadCredentialsException(e.getMessage());
		} catch (IOException e) {
			SecurityContextHolder.clearContext();
			req.setAttribute("exception", new InvalidAuthenticationException(BaseResponseStatus.AUTH_REQUEST_BODY_INVALID));
			throw new BadCredentialsException(BaseResponseStatus.AUTH_REQUEST_BODY_INVALID.getMessage());
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
		MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
		Long userId = principal.getMemberId();
		Role role = principal.getRole();
		String email = principal.getUsername();
		String ip = extractClientIp(request);

		// ✅ 실패 기록 초기화
		rateLimiter.resetLoginFailures(email, ip);

		tokenService.invalidatePreviousTokens(request);
		tokenService.generateTokensAndSetCookies(response, userId, role);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		response.setStatus(HttpStatus.OK.value());

		log.info("로그인 성공: userId={}, role={}, ip={}", userId, role, ip);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
		log.error("인증 실패: {}", failed.getMessage());
		log.error("인증 예외 유형: {}", failed.getClass().getSimpleName());

		try {
			Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), new TypeReference<>() {});
			String email = credentials.get("username");
			String ip = extractClientIp(request);

			rateLimiter.recordLoginFailure(email, ip);
		} catch (Exception ex) {
			log.warn("로그인 실패 기록 중 예외 발생", ex);
		}

		Exception ex = (Exception) request.getAttribute("exception");
		BaseResponse<Empty> errorResponse =
			(ex instanceof InvalidAuthenticationException iae)
				? BaseResponse.error(iae.getStatus())
				: (ex instanceof InvalidRedisException ire)
				? BaseResponse.error(ire.getStatus())
				: BaseResponse.error(BaseResponseStatus.UNAUTHORIZED);

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(errorResponse.getHttpStatusCode());
		objectMapper.writeValue(response.getWriter(), errorResponse);
	}
}