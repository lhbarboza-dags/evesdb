package org.sibol.eves.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/** All secrets and environment-specific values that the ZK app had hard-coded in controllers. */
@ConstructorBinding
@ConfigurationProperties("app")
public record AppProperties(
		/** Public URL of the React app, used to build e-mail links (legacy: https://sibol-eves.herokuapp.com/). */
		String baseUrl,
		Mail mail,
		Recaptcha recaptcha,
		Maps maps,
		LegacyAes legacyAes,
		Auth auth) {

	public record Mail(String from, boolean enabled) {}

	public record Recaptcha(boolean enabled, String siteKey, String secret) {}

	/** dailyLimit: map loads per day the app allows before showing the list-only fallback (0 = no app-side limit). */
	public record Maps(String apiKey, int dailyLimit) {}

	/** Key material of the legacy org.sibol.eves.app.AES256 password cipher. */
	public record LegacyAes(String secret, String salt) {}

	/** upgradeHash: re-hash legacy AES passwords to bcrypt on successful login (keep false while the ZK app shares the DB). */
	public record Auth(boolean upgradeHash) {}
}
