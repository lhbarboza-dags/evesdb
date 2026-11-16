package org.sibol.eves.api.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.sibol.eves.api.auth.AuthDtos.*;
import org.sibol.eves.config.AppProperties;
import org.sibol.eves.data.UserRepo;
import org.sibol.eves.security.AppUser;
import org.sibol.eves.security.CurrentUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

	private final AuthService auth;
	private final SecurityContextRepository contextRepo;
	private final AppProperties props;
	private final UserRepo users;
	private final org.sibol.eves.mail.CaptchaService captcha;

	public AuthController(AuthService auth, SecurityContextRepository contextRepo, AppProperties props, UserRepo users,
			org.sibol.eves.mail.CaptchaService captcha) {
		this.captcha = captcha;
		this.auth = auth;
		this.contextRepo = contextRepo;
		this.props = props;
		this.users = users;
	}

	@GetMapping("/api/public/config")
	public PublicConfig config() {
		// the page shows the widget only when the server will actually check it
		return new PublicConfig(captcha.active(), props.recaptcha().siteKey(), props.maps().apiKey());
	}

	/** Replaces reading the "userCredential" session attribute (BannerController, *Init classes). Also the keep-alive ping. */
	@GetMapping("/api/auth/me")
	public MeResponse me() {
		return CurrentUser.get()
				.flatMap(p -> users.findById(p.getUserId()))
				.map(u -> {
					AppUser p = AuthService.toPrincipal(u);
					return new MeResponse(true, p.getUserId(), p.getEmail(), p.getFirstName(), p.getLastName(),
							p.getRole().id, p.getRole().label);
				})
				.orElse(MeResponse.anonymous());
	}

	/** LoginController #btn_login. */
	@PostMapping("/api/auth/login")
	public MeResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
		AppUser user = auth.authenticate(req);
		HttpSession old = request.getSession(false);
		if (old != null)
			old.invalidate(); // session fixation protection
		request.getSession(true);
		SecurityContext ctx = SecurityContextHolder.createEmptyContext();
		ctx.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
		SecurityContextHolder.setContext(ctx);
		contextRepo.saveContext(ctx, request, response);
		return new MeResponse(true, user.getUserId(), user.getEmail(), user.getFirstName(), user.getLastName(),
				user.getRole().id, user.getRole().label);
	}

	/** BannerController #menuitem_user (logout). */
	@PostMapping("/api/auth/logout")
	public void logout(HttpServletRequest request) {
		HttpSession s = request.getSession(false);
		if (s != null)
			s.invalidate();
		SecurityContextHolder.clearContext();
	}

	@PostMapping("/api/auth/register")
	public MessageResponse register(@Valid @RequestBody RegisterRequest req) {
		auth.register(req);
		return new MessageResponse(true, "Email sent successfully.");
	}

	@GetMapping("/api/auth/verify")
	public MessageResponse verify(@RequestParam(required = false) String token) {
		return auth.verifyEmail(token);
	}

	@PostMapping("/api/auth/forgot")
	public MessageResponse forgot(@Valid @RequestBody ForgotRequest req) {
		auth.forgotPassword(req);
		return new MessageResponse(true, "Email sent successfully.");
	}

	@GetMapping("/api/auth/reset")
	public MessageResponse resetCheck(@RequestParam(required = false) String token) {
		return auth.resetTokenValid(token) ? new MessageResponse(true, "") : new MessageResponse(false, "Key does not exist.");
	}

	@PostMapping("/api/auth/reset")
	public MessageResponse reset(@Valid @RequestBody ResetRequest req) {
		auth.resetPassword(req);
		return new MessageResponse(true, "Your password has been successfully changed!");
	}

	@GetMapping("/api/auth/invitation")
	public MessageResponse acceptInvitation(@RequestParam(required = false) String token) {
		return auth.acceptInvitation(token);
	}
}
