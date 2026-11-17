package org.sibol.eves.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Lets the app use the standard Postgres connection string that Neon, Render and most hosts provide
 * ({@code DATABASE_URL=postgresql://user:pass@host/db?sslmode=require&channel_binding=require}) instead of the
 * Spring-specific JDBC variables. Used only when SPRING_DATASOURCE_URL is not set.
 *
 * DATABASE_URL_UNPOOLED (direct endpoint) is preferred when present; a pooled (PgBouncer) host gets prepareThreshold=0 so
 * the JDBC driver doesn't rely on server-side prepared statements. Parameters the bundled pgjdbc 42.3 driver doesn't support, such as
 * channel_binding, are dropped; sslmode is kept (default require).
 * Runs before {@link RequiredEnvCheck}. Registered in META-INF/spring.factories.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	static final String SOURCE_NAME = "databaseUrl";

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 10; // after config data (application.yml), before RequiredEnvCheck
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
		if (notBlank(safe(env, "spring.datasource.url")))
			return; // SPRING_DATASOURCE_URL given explicitly: it wins
		String var = notBlank(safe(env, "DATABASE_URL_UNPOOLED")) ? "DATABASE_URL_UNPOOLED"
				: notBlank(safe(env, "DATABASE_URL")) ? "DATABASE_URL" : null;
		if (var == null)
			return;
		Map<String, Object> props = convert(safe(env, var), safe(env, "SPRING_DATASOURCE_USERNAME"),
				safe(env, "SPRING_DATASOURCE_PASSWORD"));
		env.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, props));
		String host = String.valueOf(props.get("spring.datasource.url"));
		System.out.println("Database: using " + var + " (converted to JDBC" + (host.contains("-pooler")
				? ", pooled endpoint: prepareThreshold=0)" : ")"));
	}

	/** postgres[ql]://user:pass@host[:port]/db?params → spring.datasource.url / username / password. */
	static Map<String, Object> convert(String url, String explicitUser, String explicitPassword) {
		URI u = URI.create(url.trim());
		String scheme = u.getScheme() == null ? "" : u.getScheme().toLowerCase();
		if (!scheme.equals("postgres") && !scheme.equals("postgresql"))
			throw new IllegalStateException("DATABASE_URL must start with postgresql:// (got scheme '" + scheme + "')");
		String db = u.getPath() == null ? "" : u.getPath().replaceFirst("^/", "");
		String sslmode = "require";
		if (u.getRawQuery() != null)
			for (String kv : u.getRawQuery().split("&")) {
				String[] p = kv.split("=", 2);
				if (p[0].equals("sslmode") && p.length == 2 && !p[1].isBlank())
					sslmode = decode(p[1]);
			}
		String jdbc = "jdbc:postgresql://" + u.getHost() + (u.getPort() > 0 ? ":" + u.getPort() : "") + "/" + db
				+ "?sslmode=" + sslmode;
		// Pooled endpoints (PgBouncer, transaction mode) can't keep server-side prepared statements across
		// transactions; prepareThreshold=0 makes pgjdbc use unnamed statements, which works with them.
		if (u.getHost() != null && u.getHost().contains("-pooler"))
			jdbc += "&prepareThreshold=0";
		Map<String, Object> props = new HashMap<>();
		props.put("spring.datasource.url", jdbc);
		String userInfo = u.getRawUserInfo();
		if (userInfo != null) {
			String[] up = userInfo.split(":", 2);
			if (!notBlank(explicitUser))
				props.put("spring.datasource.username", decode(up[0]));
			if (up.length == 2 && !notBlank(explicitPassword))
				props.put("spring.datasource.password", decode(up[1]));
		}
		return props;
	}

	private static String safe(ConfigurableEnvironment env, String key) {
		try {
			return env.getProperty(key);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static String decode(String s) {
		return URLDecoder.decode(s, StandardCharsets.UTF_8);
	}

	private static boolean notBlank(String s) {
		return s != null && !s.isBlank();
	}
}
