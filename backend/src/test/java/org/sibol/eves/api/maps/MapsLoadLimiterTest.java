package org.sibol.eves.api.maps;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class MapsLoadLimiterTest {

	/** Mutable clock for the day rollover. */
	static class TestClock extends Clock {
		Instant now = Instant.parse("2026-11-17T12:00:00Z");

		@Override
		public ZoneId getZone() {
			return MapsLoadLimiter.GOOGLE_QUOTA_ZONE;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}

	@Test
	void allowsUpToLimitThenDeniesAndResetsNextDay() {
		TestClock clock = new TestClock();
		MapsLoadLimiter l = new MapsLoadLimiter(2, clock);
		assertThat(l.tryAcquire()).isTrue();
		assertThat(l.tryAcquire()).isTrue();
		assertThat(l.tryAcquire()).isFalse();
		clock.now = clock.now.plus(Duration.ofDays(1));
		assertThat(l.tryAcquire()).isTrue();
	}

	@Test
	void zeroMeansUnlimited() {
		MapsLoadLimiter l = new MapsLoadLimiter(0, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
		for (int i = 0; i < 1000; i++)
			assertThat(l.tryAcquire()).isTrue();
	}
}
