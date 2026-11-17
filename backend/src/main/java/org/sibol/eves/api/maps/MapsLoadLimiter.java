package org.sibol.eves.api.maps;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.sibol.eves.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * App-side daily cap on Google map loads, so visitors get a friendly list-only fallback before the Google Cloud quota
 * ("Map loads per day") is reached. The Google quota stays the hard stop: this counter is in memory and restarts at 0
 * when the app restarts. Days follow Pacific time, like Google's quota reset.
 */
@Component
public class MapsLoadLimiter {

	static final ZoneId GOOGLE_QUOTA_ZONE = ZoneId.of("America/Los_Angeles");

	private final int limit;
	private final Clock clock;
	private LocalDate day;
	private int count;

	@Autowired
	public MapsLoadLimiter(AppProperties props) {
		this(props.maps().dailyLimit(), Clock.system(GOOGLE_QUOTA_ZONE));
	}

	MapsLoadLimiter(int limit, Clock clock) {
		this.limit = limit;
		this.clock = clock;
	}

	/** Counts one map load; false once today's limit is used up (limit <= 0 means unlimited). */
	public synchronized boolean tryAcquire() {
		LocalDate today = LocalDate.now(clock);
		if (!today.equals(day)) {
			day = today;
			count = 0;
		}
		if (limit > 0 && count >= limit)
			return false;
		count++;
		return true;
	}
}
