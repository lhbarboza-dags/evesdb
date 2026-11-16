package org.sibol.eves.api.auth;

import java.util.List;

import org.sibol.eves.api.auth.AuthDtos.*;
import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.data.UserRepo;
import org.sibol.eves.mail.CaptchaService;
import org.sibol.eves.mail.MailService;
import org.sibol.eves.security.AppUser;
import org.sibol.eves.security.PasswordService;
import org.sibol.eves.security.Role;
import org.sibol.eves.security.Tokens;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.User;

/**
 * Ports AuthenticationService, RegisterController, forgotPasswordController, ResetPasswordController,
 * ValidateController and ValidateEmailController.
 */
@Service
@Transactional
public class AuthService {

	private final UserRepo users;
	private final PasswordService passwords;
	private final MailService mail;
	private final CaptchaService captcha;

	public AuthService(UserRepo users, PasswordService passwords, MailService mail, CaptchaService captcha) {
		this.users = users;
		this.passwords = passwords;
		this.mail = mail;
		this.captcha = captcha;
	}

	/** AuthenticationService.login: e-mail must be verified, password must match. */
	public AppUser authenticate(LoginRequest req) {
		User u = users.findOneByEmail(req.email())
				.orElseThrow(() -> ApiException.unauthorized("Invalid email or password."));
		if (!passwords.matches(req.password(), u.getPassword()))
			throw ApiException.unauthorized("Invalid email or password.");
		if (Tokens.blocksLogin(u.getValidation()))
			throw ApiException.unauthorized("Email is not yet validated. Please click the link we sent to your email.");
		if (passwords.needsUpgrade(u.getPassword())) {
			u.setPassword(passwords.encode(req.password()));
			users.save(u);
		}
		return toPrincipal(u);
	}

	public static AppUser toPrincipal(User u) {
		return new AppUser(u.getUserid(), u.getEmailadd(), u.getFirstname(), u.getLastname(), Role.of(u.getRole()));
	}

	/** RegisterController.buttonSubmit (+ AgreementController: terms must be accepted, + reCAPTCHA re-checked). */
	public void register(RegisterRequest req) {
		if (!req.agreedToTerms())
			throw ApiException.badRequest("You must agree to the Terms of Use.");
		if (!req.password().equals(req.confirmPassword()))
			throw ApiException.badRequest("Passwords do not match.");
		captcha.verify(req.captcha());
		String email = req.email().trim().toLowerCase();
		if (users.findOneByEmail(email).isPresent())
			throw ApiException.conflict("Email exists.");
		User u = new User();
		u.setEmailadd(email);
		u.setLastname(req.lastName().trim());
		u.setFirstname(req.firstName().trim());
		u.setInstitution(nz(req.institution()));
		u.setSector(nz(req.sector()));
		u.setPurpose(nz(req.purpose()));
		u.setPassword(passwords.encode(req.password()));
		u.setRole(Role.USER.id);
		String token = Tokens.newVerification();
		u.setValidation(token);
		users.save(u);
		mail.send(List.of(email), null, "SIBOL-EVES-DB: Verification",
				"Hello,\n You registered an account on SIBOL-EVES Database, before being able to use your account you need "
						+ "to verify that this is your email address by clicking here:\n"
						+ mail.link("/verify-email?token=" + token) + "\n\n Kind Regards");
	}

	/** ValidateController. */
	public MessageResponse verifyEmail(String token) {
		if (token == null || token.isBlank() || Tokens.isReset(token))
			return new MessageResponse(false, "Validation key does not exist.");
		List<User> found = users.findByValidation(token);
		if (found.isEmpty())
			return new MessageResponse(false, "Validation key does not exist.");
		User u = found.get(0);
		u.setValidation("");
		users.save(u);
		return new MessageResponse(true, "Email validation successful.");
	}

	/** forgotPasswordController.buttonSubmit — fixed: the mail now goes to the user, not a hard-coded address. */
	public void forgotPassword(ForgotRequest req) {
		captcha.verify(req.captcha());
		User u = users.findOneByEmail(req.email())
				.orElseThrow(() -> ApiException.notFound("Email does not exist"));
		if (Tokens.blocksLogin(u.getValidation()))
			throw ApiException.badRequest("Email is not yet validated. Please verify your email first.");
		String token = Tokens.newReset();
		u.setValidation(token);
		users.save(u);
		mail.send(List.of(u.getEmailadd()), null, "SIBOL-EVES-DB: FORGOT PASSWORD",
				"Hi,\nThere was a request to change your password!\nIf you did not make this request then please ignore this email."
						+ "\nOtherwise, please click this link to change your password:\n "
						+ mail.link("/reset-password?token=" + token));
	}

	/** ResetPasswordController init: is the key valid? */
	public boolean resetTokenValid(String token) {
		return token != null && Tokens.isReset(token) && !users.findByValidation(token).isEmpty();
	}

	/** ResetPasswordController.buttonSubmit. */
	public void resetPassword(ResetRequest req) {
		if (!req.password().equals(req.confirmPassword()))
			throw ApiException.badRequest("Passwords do not match.");
		if (!resetTokenValid(req.token()))
			throw ApiException.notFound("Key does not exist.");
		User u = users.findByValidation(req.token()).get(0);
		u.setValidation("");
		u.setPassword(passwords.encode(req.password()));
		users.save(u);
	}

	/** ValidateEmailController: accept an invitation to receive Contact-Us mails of one or more categories. */
	public MessageResponse acceptInvitation(String token) {
		if (token == null || token.isBlank())
			return new MessageResponse(false, "No Key Found.");
		boolean found = false;
		for (User u : users.findAll()) {
			boolean changed = false;
			if (token.equals(u.getBugreport())) { u.setBugreport(Tokens.ACCEPTED); changed = true; }
			if (token.equals(u.getImprovement())) { u.setImprovement(Tokens.ACCEPTED); changed = true; }
			if (token.equals(u.getInquiry())) { u.setInquiry(Tokens.ACCEPTED); changed = true; }
			if (token.equals(u.getSuggestastudy())) { u.setSuggestastudy(Tokens.ACCEPTED); changed = true; }
			if (changed) {
				users.save(u);
				found = true;
			}
		}
		return found ? new MessageResponse(true, "Email validation successful.") : new MessageResponse(false, "No Key Found.");
	}

	private static String nz(String s) {
		return s == null ? "" : s.trim();
	}
}
