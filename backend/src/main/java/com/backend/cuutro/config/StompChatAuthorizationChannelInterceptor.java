package com.backend.cuutro.config;

import java.security.Principal;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.backend.cuutro.services.PhieuCuuTroService;

@Component
public class StompChatAuthorizationChannelInterceptor implements ChannelInterceptor {

	private static final Pattern CHAT_TOPIC_PATTERN = Pattern.compile("^/topic/phieu-cuu-tro/(\\d+)/tin-nhan(?:/.*)?$");

	private final PhieuCuuTroService phieuCuuTroService;

	public StompChatAuthorizationChannelInterceptor(@Lazy PhieuCuuTroService phieuCuuTroService) {
		this.phieuCuuTroService = phieuCuuTroService;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}
		if (!StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			return message;
		}

		String destination = accessor.getDestination();
		if (!StringUtils.hasText(destination)) {
			return message;
		}

		Matcher matcher = CHAT_TOPIC_PATTERN.matcher(destination);
		if (!matcher.matches()) {
			return message;
		}

		Authentication authentication = resolveAuthentication(accessor.getUser());
		Long phieuId = Long.parseLong(matcher.group(1));
		Long taiKhoanId = resolveTaiKhoanId(authentication);
		Set<String> roles = resolveRoles(authentication);
		phieuCuuTroService.validateCoQuyenChat(phieuId, taiKhoanId, roles);
		return message;
	}

	private Authentication resolveAuthentication(Principal principal) {
		if (!(principal instanceof Authentication authentication)) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized");
		}
		return authentication;
	}

	private Long resolveTaiKhoanId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof Jwt jwt)) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized");
		}
		Object claim = jwt.getClaim("taiKhoanId");
		if (claim instanceof Number number) {
			return number.longValue();
		}
		if (claim instanceof String text && StringUtils.hasText(text)) {
			return Long.parseLong(text);
		}
		throw new AuthenticationCredentialsNotFoundException("Invalid token");
	}

	private Set<String> resolveRoles(Authentication authentication) {
		return authentication.getAuthorities()
				.stream()
				.map(grantedAuthority -> grantedAuthority.getAuthority())
				.filter(StringUtils::hasText)
				.collect(Collectors.toSet());
	}
}
