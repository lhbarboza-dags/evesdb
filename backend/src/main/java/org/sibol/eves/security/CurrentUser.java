package org.sibol.eves.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
	private CurrentUser() {}

	public static Optional<AppUser> get() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		if (a != null && a.getPrincipal() instanceof AppUser u)
			return Optional.of(u);
		return Optional.empty();
	}

	public static AppUser require() {
		return get().orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Not logged in"));
	}
}
