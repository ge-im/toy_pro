package com.example.demo.config.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI commentOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("toy_pro_comment API")
				.version("v1")
				.description("Comment RouterFunction API documentation"));
	}
}
