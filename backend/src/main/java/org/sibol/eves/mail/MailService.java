package org.sibol.eves.mail;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collection;

import javax.mail.AuthenticationFailedException;
import javax.mail.SendFailedException;

import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

/** Single mail sender replacing the five copies of javax.mail code (Register, forgotPassword, SendMail, SaveMail, UserMgt). */
@Service
public class MailService {
	private static final Logger log = LoggerFactory.getLogger(MailService.class);

	private final JavaMailSender sender;
	private final AppProperties props;

	public MailService(JavaMailSender sender, AppProperties props) {
		this.sender = sender;
		this.props = props;
	}

	public void send(Collection<String> to, String replyTo, String subject, String body) {
		if (to == null || to.isEmpty())
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "No recipient is configured for this message.");
		if (!props.mail().enabled()) {
			log.warn("MAIL_ENABLED=false, not sending '{}' to {}:\n{}", subject, to, body);
			return;
		}
		SimpleMailMessage m = new SimpleMailMessage();
		m.setFrom(props.mail().from());
		m.setTo(to.toArray(String[]::new));
		if (replyTo != null && !replyTo.isBlank())
			m.setReplyTo(replyTo);
		m.setSubject(subject);
		m.setText(body);
		try {
			sender.send(m);
		} catch (MailException e) {
			log.error("Mail failed", e);
			log.warn("Mail diagnosis: {}", diagnose(e, host(), port()));
			throw new ApiException(HttpStatus.BAD_GATEWAY, "The e-mail could not be sent. Please try again later.");
		}
	}

	/** One startup line so missing SMTP settings are visible before the first e-mail fails. Never logs secrets. */
	@EventListener(ApplicationReadyEvent.class)
	public void logConfiguration() {
		boolean enabled = props.mail().enabled();
		String user = sender instanceof JavaMailSenderImpl i ? i.getUsername() : null;
		String pass = sender instanceof JavaMailSenderImpl i ? i.getPassword() : null;
		log.info("Mail: enabled={}, host={}:{}, username={}, password={}, from={}", enabled, host(), port(),
				blank(user) ? "EMPTY" : "set", blank(pass) ? "EMPTY" : "set", props.mail().from());
		if (!enabled)
			return;
		if (blank(user) || blank(pass))
			log.warn("Mail: SMTP_USERNAME (or SMTP_USER) and/or SMTP_PASSWORD (or SMTP_KEY) are not set; sending e-mail will fail. See deploy/README-FREE.md B7.");
		int port = port();
		if (port == 25 || port == 465 || port == 587)
			log.warn("Mail: port {} is blocked on Render's free plan; use SMTP_PORT=2525 (Brevo). See deploy/README-FREE.md B7.", port);
	}

	/** Plain-language reason for a mail failure, from the exception's cause chain. */
	static String diagnose(Throwable e, String host, int port) {
		for (Throwable t = e; t != null; t = t.getCause() == t ? null : t.getCause()) {
			if (t instanceof AuthenticationFailedException)
				return "SMTP login rejected or missing: check SMTP_USERNAME (the SMTP login) and SMTP_PASSWORD (the SMTP key).";
			if (t instanceof SocketTimeoutException || t instanceof ConnectException
					|| t.getClass().getSimpleName().equals("MailConnectException"))
				return "cannot reach " + host + ":" + port + " (blocked port or wrong host; on Render free use port 2525).";
			if (t instanceof UnknownHostException)
				return "unknown SMTP host " + host + ": check SMTP_HOST.";
			if (t instanceof SendFailedException)
				return "the server refused the message: " + t.getMessage()
						+ " (usually MAIL_FROM is not a verified sender, or the account is not activated).";
		}
		return String.valueOf(e.getMessage());
	}

	private String host() {
		return sender instanceof JavaMailSenderImpl i ? i.getHost() : "?";
	}

	private int port() {
		return sender instanceof JavaMailSenderImpl i ? i.getPort() : -1;
	}

	private static boolean blank(String s) {
		return s == null || s.isBlank();
	}

	public String link(String pathAndQuery) {
		String base = props.baseUrl().endsWith("/") ? props.baseUrl().substring(0, props.baseUrl().length() - 1) : props.baseUrl();
		return base + pathAndQuery;
	}
}
