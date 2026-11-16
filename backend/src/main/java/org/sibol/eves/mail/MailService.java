package org.sibol.eves.mail;

import java.util.Collection;

import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
			throw new ApiException(HttpStatus.BAD_GATEWAY, "The e-mail could not be sent. Please try again later.");
		}
	}

	public String link(String pathAndQuery) {
		String base = props.baseUrl().endsWith("/") ? props.baseUrl().substring(0, props.baseUrl().length() - 1) : props.baseUrl();
		return base + pathAndQuery;
	}
}
