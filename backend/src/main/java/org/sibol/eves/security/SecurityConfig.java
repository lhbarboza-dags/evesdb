package org.sibol.eves.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Session-cookie security for the SPA (same origin through nginx). Replaces the ZK page initiators
 * SearchInit (login required), DataMgtInit (Curator+) and UserMgtInit (Admin); finer rules use @PreAuthorize.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository repo) throws Exception {
		http.securityContext(sc -> sc.securityContextRepository(repo))
				.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
				.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
				.authorizeRequests(auth -> auth
						.antMatchers("/api/auth/**", "/api/public/**", "/api/contact", "/actuator/health").permitAll()
						.antMatchers("/api/**").authenticated()
						.anyRequest().permitAll())
				.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.formLogin(f -> f.disable())
				.httpBasic(b -> b.disable())
				.logout(l -> l.disable());
		return http.build();
	}

	/** Forces the XSRF-TOKEN cookie to be written on every response so the SPA can echo it in X-XSRF-TOKEN. */
	static class CsrfCookieFilter extends OncePerRequestFilter {
		@Override
		protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
				throws ServletException, IOException {
			CsrfToken token = (CsrfToken) req.getAttribute(CsrfToken.class.getName());
			if (token != null)
				token.getToken();
			chain.doFilter(req, res);
		}
	}
}
