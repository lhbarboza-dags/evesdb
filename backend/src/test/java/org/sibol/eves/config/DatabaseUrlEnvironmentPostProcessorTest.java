package org.sibol.eves.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class DatabaseUrlEnvironmentPostProcessorTest {

	private final DatabaseUrlEnvironmentPostProcessor pp = new DatabaseUrlEnvironmentPostProcessor();

	@Test
	void neonStyleUrlBecomesJdbcWithoutChannelBinding() {
		Map<String, Object> p = DatabaseUrlEnvironmentPostProcessor.convert(
				"postgresql://neondb_owner:s3cr%40t@ep-cool-1.c-3.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require",
				null, null);
		assertThat(p.get("spring.datasource.url"))
				.isEqualTo("jdbc:postgresql://ep-cool-1.c-3.ap-southeast-1.aws.neon.tech/neondb?sslmode=require");
		assertThat(p.get("spring.datasource.username")).isEqualTo("neondb_owner");
		assertThat(p.get("spring.datasource.password")).isEqualTo("s3cr@t");
	}

	@Test
	void keepsPortAndDefaultsSslmode() {
		Map<String, Object> p = DatabaseUrlEnvironmentPostProcessor.convert("postgres://u:p@db.example.org:6543/app", null, null);
		assertThat(p.get("spring.datasource.url")).isEqualTo("jdbc:postgresql://db.example.org:6543/app?sslmode=require");
	}

	@Test
	void pooledHostDisablesServerPreparedStatements() {
		Map<String, Object> p = DatabaseUrlEnvironmentPostProcessor.convert(
				"postgresql://u:p@ep-a-pooler.c-3.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require", null, null);
		assertThat(p.get("spring.datasource.url"))
				.isEqualTo("jdbc:postgresql://ep-a-pooler.c-3.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&prepareThreshold=0");
	}

	@Test
	void explicitUserAndPasswordWin() {
		Map<String, Object> p = DatabaseUrlEnvironmentPostProcessor.convert("postgresql://u:p@h/db", "other", "pw");
		assertThat(p).doesNotContainKeys("spring.datasource.username", "spring.datasource.password");
	}

	@Test
	void explicitJdbcUrlWins() {
		MockEnvironment env = new MockEnvironment().withProperty("spring.datasource.url", "jdbc:postgresql://x/y")
				.withProperty("DATABASE_URL", "postgresql://u:p@h/db");
		pp.postProcessEnvironment(env, null);
		assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://x/y");
	}

	@Test
	void unpooledPreferredAndApplied() {
		MockEnvironment env = new MockEnvironment().withProperty("spring.datasource.url", "")
				.withProperty("DATABASE_URL", "postgresql://u:p@ep-a-pooler.neon.tech/db?sslmode=require")
				.withProperty("DATABASE_URL_UNPOOLED", "postgresql://u:p@ep-a.neon.tech/db?sslmode=require");
		pp.postProcessEnvironment(env, null);
		assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://ep-a.neon.tech/db?sslmode=require");
		assertThat(env.getProperty("spring.datasource.password")).isEqualTo("p");
	}
}
