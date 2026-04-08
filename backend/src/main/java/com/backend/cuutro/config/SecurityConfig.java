package com.backend.cuutro.config;

import javax.crypto.spec.SecretKeySpec;

import com.backend.cuutro.constant.ConstantVariables;
import com.backend.cuutro.constant.enums.RoleType;
import com.nimbusds.jose.JWSAlgorithm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/auth/login",
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/hello").permitAll()
						.requestMatchers(HttpMethod.POST, "/phieu-cuu-tro", "/tep-tin/upload").permitAll()
						.requestMatchers("/don-vi/**", "/doi-nhom/**", "/loai-su-co/**", "/nhom-vat-pham/**", "/vat-pham/**")
						.hasAuthority(RoleType.ADMIN.name())
						.requestMatchers(HttpMethod.POST, "/phieu-cuu-tro/*/dieu-phoi")
						.hasAuthority(RoleType.ADMIN.name())
						.requestMatchers(HttpMethod.POST, "/phieu-cuu-tro/*/nhan-nhiem-vu")
						.hasAuthority(RoleType.TRUONG_NHOM_TNV.name())
						.requestMatchers(HttpMethod.PUT, "/phieu-cuu-tro/*/trang-thai")
						.hasAuthority(RoleType.TRUONG_NHOM_TNV.name())
						.requestMatchers(HttpMethod.POST, "/phieu-cuu-tro/*/tin-nhan")
						.hasAnyAuthority(RoleType.NGUOI_DAN.name(), RoleType.TRUONG_NHOM_TNV.name())
						.requestMatchers(HttpMethod.GET, "/phieu-cuu-tro/**")
						.hasAnyAuthority(RoleType.ADMIN.name(), RoleType.NGUOI_DAN.name(), RoleType.TRUONG_NHOM_TNV.name())
						.requestMatchers(HttpMethod.POST, "/tinh-nguyen-vien/dang-ky")
						.hasAnyAuthority(RoleType.NGUOI_DAN.name(), RoleType.ADMIN.name())
						.requestMatchers(HttpMethod.PUT, "/tinh-nguyen-vien/*/duyet")
						.hasAuthority(RoleType.ADMIN.name())
						.requestMatchers(HttpMethod.DELETE, "/tinh-nguyen-vien/**")
						.hasAuthority(RoleType.ADMIN.name())
						.requestMatchers(HttpMethod.GET, "/tinh-nguyen-vien/**")
						.hasAnyAuthority(RoleType.ADMIN.name(), RoleType.TRUONG_NHOM_TNV.name())
						.requestMatchers(HttpMethod.POST, "/tinh-nguyen-vien/gan-doi-nhom")
						.hasAnyAuthority(RoleType.ADMIN.name(), RoleType.TRUONG_NHOM_TNV.name())
						.anyRequest().authenticated())
				.csrf(AbstractHttpConfigurer::disable)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.decoder(jwtDecoder())
								.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		SecretKeySpec secretKeySpec = new SecretKeySpec(
				ConstantVariables.SIGNER_KEY.getBytes(),
				JWSAlgorithm.HS256.toString());
		return NimbusJwtDecoder.withSecretKey(secretKeySpec)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
		grantedAuthoritiesConverter.setAuthorityPrefix("");

		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("*")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*");
	}
}
