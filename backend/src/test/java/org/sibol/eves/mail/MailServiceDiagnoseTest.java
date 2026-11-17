package org.sibol.eves.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.SocketTimeoutException;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

class MailServiceDiagnoseTest {

	@Test
	void missingPassword() {
		var e = new MailAuthenticationException(new AuthenticationFailedException("failed to connect, no password specified?"));
		assertThat(MailService.diagnose(e, "smtp-relay.brevo.com", 2525)).contains("SMTP_USERNAME").contains("SMTP_PASSWORD");
	}

	@Test
	void unreachable() {
		var e = new MailSendException("x", new MessagingException("connect", new SocketTimeoutException("connect timed out")));
		assertThat(MailService.diagnose(e, "smtp.gmail.com", 587)).startsWith("cannot reach smtp.gmail.com:587");
	}

	@Test
	void refusedSender() {
		var e = new MailSendException("x", new SendFailedException("550 sender not valid"));
		assertThat(MailService.diagnose(e, "h", 2525)).contains("550 sender not valid").contains("MAIL_FROM");
	}
}
