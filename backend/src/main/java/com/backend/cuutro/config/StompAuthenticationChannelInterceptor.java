package com.backend.cuutro.config;

import java.util.Locale;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompAuthenticationChannelInterceptor implements ChannelInterceptor {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String AUTHORIZATION_HEADER_LOWERCASE = "authorization";
	private static final String BEARER_PREFIX = "bearer ";

	private final JwtDecoder jwtDecoder;
	private final JwtAuthenticationConverter jwtAuthenticationConverter;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String authorizationHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
			if (!StringUtils.hasText(authorizationHeader)) {
				authorizationHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER_LOWERCASE);
			}
			String token = resolveBearerToken(authorizationHeader);
			Jwt jwt = jwtDecoder.decode(token);
			Authentication authentication = jwtAuthenticationConverter.convert(jwt);
			if (authentication == null) {
				throw new AuthenticationCredentialsNotFoundException("Unauthorized");
			}
			accessor.setUser(authentication);
			return message;
		}

		if ((StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))
				&& accessor.getUser() == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized");
		}

		return message;
	}

	private String resolveBearerToken(String authorizationHeader) {
		if (!StringUtils.hasText(authorizationHeader)) {
			throw new AuthenticationCredentialsNotFoundException("Missing Authorization header");
		}
		String normalized = authorizationHeader.trim();
		if (!normalized.toLowerCase(Locale.ROOT).startsWith(BEARER_PREFIX)) {
			throw new AuthenticationCredentialsNotFoundException("Authorization header must use Bearer token");
		}
		String token = normalized.substring(BEARER_PREFIX.length()).trim();
		if (!StringUtils.hasText(token)) {
			throw new AuthenticationCredentialsNotFoundException("Bearer token is required");
		}
		return token;
	}
}
