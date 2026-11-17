package org.sibol.eves.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Single-container deployment (root Dockerfile): the React build is packaged in classpath:/static and served by
 * Spring Boot. Client-side routes (/search, /studies/new, …) and legacy *.zul links have no static file, so GET
 * requests for them are forwarded to /index.html and React Router takes over. /api/** and /actuator/** are untouched,
 * and real files (anything with an extension, e.g. /assets/x.js, /img/a.webp) are served as-is.
 *
 * Harmless when the SPA is served by nginx instead (docker-compose setups): requests never reach this filter then.
 */
@Component
public class SpaForwardFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		if (isSpaRoute(req)) {
			req.getRequestDispatcher("/index.html").forward(req, res);
			return;
		}
		chain.doFilter(req, res);
	}

	static boolean isSpaRoute(HttpServletRequest req) {
		if (!"GET".equals(req.getMethod()))
			return false;
		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (path.isEmpty() || path.equals("/") || path.startsWith("/api/") || path.equals("/api")
				|| path.startsWith("/actuator") || path.equals("/index.html") || path.equals("/error"))
			return false;
		String last = path.substring(path.lastIndexOf('/') + 1);
		return !last.contains(".") || last.endsWith(".zul");
	}
}
