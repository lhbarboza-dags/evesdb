package org.sibol.eves.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails fast, with one readable line, when required settings are missing (e.g. environment variables not added on
 * Render). Without this the app silently tried localhost:5432 and died with a 200-line stack trace.
 * Registered in META-INF/spring.factories.
 */
public class RequiredEnvCheck implements EnvironmentPostProcessor {

	private static final String HELP = " See deploy/README-FREE.md (section B3) for the full list of environment variables.";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
		List<String> missing = new ArrayList<>();
		check(env, "spring.datasource.url", "SPRING_DATASOURCE_URL (JDBC URL, e.g. jdbc:postgresql://<host>/<db>?sslmode=require)", missing);
		check(env, "spring.datasource.username", "SPRING_DATASOURCE_USERNAME", missing);
		check(env, "app.legacy-aes.secret", "LEGACY_AES_SECRET", missing);
		check(env, "app.legacy-aes.salt", "LEGACY_AES_SALT", missing);
		if (!missing.isEmpty()) {
			String msg = "Missing required configuration: " + String.join(", ", missing) + "." + HELP;
			System.err.println("\n*** " + msg + "\n");
			throw new IllegalStateException(msg);
		}
	}

	private static void check(ConfigurableEnvironment env, String key, String label, List<String> missing) {
		String v;
		try {
			v = env.getProperty(key);
		} catch (IllegalArgumentException unresolvedPlaceholder) {
			v = null; // e.g. ${LEGACY_AES_SECRET} without a value
		}
		if (v == null || v.isBlank())
			missing.add(label);
	}
}
