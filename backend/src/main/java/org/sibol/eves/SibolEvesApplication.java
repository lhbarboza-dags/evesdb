package org.sibol.eves;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot entry point. Replaces the ZK WAR (web.xml + ApplicationContext.xml + AppConfig).
 * Entities stay in the legacy {@code model} package and repositories/services keep their packages and bean names.
 */
@SpringBootApplication
@EntityScan("model")
@EnableJpaRepositories({ "org.sibol.eves.repositories", "org.sibol.eves.data" })
@ConfigurationPropertiesScan
public class SibolEvesApplication {
	public static void main(String[] args) {
		SpringApplication.run(SibolEvesApplication.class, args);
	}
}
