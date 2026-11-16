package org.sibol.eves.api.contact;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.sibol.eves.api.auth.AuthDtos.MessageResponse;
import org.sibol.eves.api.users.MailCategory;
import org.sibol.eves.data.UserRepo;
import org.sibol.eves.mail.CaptchaService;
import org.sibol.eves.mail.MailService;
import org.sibol.eves.security.Tokens;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** contactUs.zul → SendMailController (public page). Fixed: validation and server-side reCAPTCHA. */
@RestController
public class ContactController {

	public record ContactRequest(@NotNull MailCategory category, @NotBlank @Size(max = 100) String name,
			@NotBlank @Email(regexp = ".+@.+\\..+") String email, @Size(max = 500) String organization,
			@NotBlank @Size(max = 5000) String message, String captcha) {}

	private final UserRepo users;
	private final MailService mail;
	private final CaptchaService captcha;

	public ContactController(UserRepo users, MailService mail, CaptchaService captcha) {
		this.users = users;
		this.mail = mail;
		this.captcha = captcha;
	}

	@PostMapping("/api/contact")
	public MessageResponse send(@Valid @RequestBody ContactRequest req) {
		captcha.verify(req.captcha());
		List<String> to = users.findAll().stream().filter(u -> Tokens.ACCEPTED.equals(req.category().getter.apply(u)))
				.map(u -> u.getEmailadd()).toList();
		mail.send(to, req.email(), "SIBOL-EVES-DB: " + req.category().label, req.message() + "\n\nFrom: " + req.name()
				+ " <" + req.email() + ">\nInstitution: " + (req.organization() == null ? "" : req.organization()));
		return new MessageResponse(true, "Email sent successfully.");
	}
}
