package org.sibol.eves.api.maps;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Called by the Map tab once before it loads Google Maps (see MapsLoadLimiter). */
@RestController
public class MapsController {

	public record MapLoad(boolean allowed) {}

	private final MapsLoadLimiter limiter;

	public MapsController(MapsLoadLimiter limiter) {
		this.limiter = limiter;
	}

	@PostMapping("/api/maps/load")
	public MapLoad load() {
		return new MapLoad(limiter.tryAcquire());
	}
}
