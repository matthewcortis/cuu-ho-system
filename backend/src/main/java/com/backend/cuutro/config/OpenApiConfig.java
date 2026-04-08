package com.backend.cuutro.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI(
			@Value("${open.api.title}") String title,
			@Value("${open.api.version}") String version,
			@Value("${open.api.description}") String description,
			@Value("${open.api.local-server-url:http://localhost:8080}") String localServerUrl) {
		return new OpenAPI()
				.info(new Info()
						.title(title)
						.version(version)
						.description(description)
						.license(new License()
								.name("MIT License")
								.url("https://opensource.org/licenses/MIT")))
				.servers(List.of(
						new Server()
								.url(localServerUrl)
								.description("Local Development Server")))
				.components(new Components()
						.addSecuritySchemes("bearerAuth", new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}

	@Bean
	public GroupedOpenApi allApi() {
		return GroupedOpenApi.builder()
				.group("all")
				.packagesToScan("com.backend.cuutro.controller")
				.build();
	}

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
				.group("public")
				.pathsToMatch("/hello", "/actuator/**")
				.build();
	}

	@Bean
	public GroupedOpenApi apiV1() {
		return GroupedOpenApi.builder()
				.group("api-v1")
				.pathsToMatch("/api/v1/**")
				.build();
	}
}
