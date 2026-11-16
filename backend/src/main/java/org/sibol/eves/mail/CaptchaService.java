package org.sibol.eves.mail;

import java.util.Map;

import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.config.AppProperties;
import org.springframework.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Server-side reCAPTCHA v2 check (port of org.sibol.eves.app.RecaptchaVerifier). In ZK the result only enabled the
 * submit button; here every protected endpoint re-verifies the token.
 */
@Service
public class CaptchaService {
	private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
	private final AppProperties props;
	private final RestTemplate rest = new RestTemplate();

	private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);

	public CaptchaService(AppProperties props) {
		this.props = props;
	}

	/** Captcha is checked only when enabled AND both keys are configured (the page hides the widget without a site key). */
	public boolean active() {
		return props.recaptcha().enabled() && notBlank(props.recaptcha().siteKey()) && notBlank(props.recaptcha().secret());
	}

	public void verify(String token) {
		if (!active()) {
			if (props.recaptcha().enabled())
				log.warn("RECAPTCHA_ENABLED=true but RECAPTCHA_SITE_KEY/RECAPTCHA_SECRET are empty: captcha check skipped.");
			return;
		}
		if (token == null || token.isBlank())
			throw ApiException.badRequest("Please confirm you are not a robot.");
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("secret", props.recaptcha().secret());
		form.add("response", token);
		HttpHeaders h = new HttpHeaders();
		h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		Map<String, Object> res;
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> r = rest.postForObject(VERIFY_URL, new HttpEntity<>(form, h), Map.class);
			res = r;
		} catch (RestClientException e) {
			log.error("reCAPTCHA verification call failed", e);
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
					"The reCAPTCHA service could not be reached. Please try again later.");
		}
		if (res == null || !Boolean.TRUE.equals(res.get("success")))
			throw ApiException.badRequest("reCAPTCHA verification failed. Please try again.");
	}

	private static boolean notBlank(String s) {
		return s != null && !s.isBlank();
	}
}
