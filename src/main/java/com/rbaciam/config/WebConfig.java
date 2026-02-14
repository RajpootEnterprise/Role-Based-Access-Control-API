package com.rbaciam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://localhost:3000", "http://localhost:3001", "http://localhost:3030",
						"http://localhost:3031","http://135.148.3.213:3000/")
				.allowedMethods("GET", "POST", "PUT", "DELETE").allowCredentials(true)
				.exposedHeaders("Content-Disposition");
	}
}