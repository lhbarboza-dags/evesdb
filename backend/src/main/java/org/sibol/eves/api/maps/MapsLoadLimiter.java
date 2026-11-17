package org.sibol.eves.api.maps;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.sibol.eves.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * App-side daily cap on Google map loads, so visitors get a friendly list-only fallback before the Google Cloud quota
 * ("Map loads per day") is reached. The Google quota stays the hard stop: this counter is in memory and restarts at 0
 * when the app restarts. Days follow Pacific time, like Google's quota reset.
 */
@Component
public class MapsLoadLimiter {

	private static final Logger log = LoggerFactory.getLogger(MapsLoadLimiter.class);

	static final ZoneId GOOGLE_QUOTA_ZONE = ZoneId.of("America/Los_Angeles");

	private final int limit;
	private final Clock clock;
	private boolean keySet;
	private LocalDate day;
	private int count;

	@Autowired
	public MapsLoadLimiter(AppProperties props) {
		this(props.maps().dailyLimit(), Clock.system(GOOGLE_QUOTA_ZONE));
		String key = props.maps().apiKey();
		this.keySet = key != null && !key.isBlank();
	}

	MapsLoadLimiter(int limit, Clock clock) {
		this.limit = limit;
		this.clock = clock;
	}

	/** One startup line so a missing or misnamed key is visible in the log. Never logs the key itself. */
	@EventListener(ApplicationReadyEvent.class)
	public void logConfiguration() {
		log.info("Maps: api key={}, app daily limit={}", keySet ? "set" : "EMPTY", limit <= 0 ? "unlimited" : limit);
		if (!keySet)
			log.warn("Maps: no Google Maps key, so the Map tab shows only the site list. Set GOOGLE_MAPS_API_KEY "
					+ "(also accepted: GOOGLE_MAPS_KEY, MAPS_API_KEY, GOOGLE_API_KEY, VITE_GOOGLE_MAPS_API_KEY).");
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
