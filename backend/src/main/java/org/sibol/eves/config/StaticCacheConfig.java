package org.sibol.eves.config;

import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Caching for the React build when Spring Boot serves it (single-container deployment):
 * /assets/** file names contain a content hash, so they are cached for a year; index.html must always be revalidated
 * so a new deploy is picked up. Other static files (img/, manual/) keep Spring's defaults.
 */
@Configuration
public class StaticCacheConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/assets/**")
				.addResourceLocations("classpath:/static/assets/")
				.setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
		registry.addResourceHandler("/index.html")
				.addResourceLocations("classpath:/static/")
				.setCacheControl(CacheControl.noCache());
	}
}
