package org.sibol.eves.api.auth;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public final class AuthDtos {
	private AuthDtos() {}

	public record LoginRequest(@NotBlank(message = "Please enter your email.") String email,
			@NotBlank(message = "Please enter your password.") String password) {}

	public record MeResponse(boolean authenticated, Integer userId, String email, String firstName, String lastName,
			Integer role, String roleName) {
		static MeResponse anonymous() { return new MeResponse(false, null, null, null, null, null, null); }
	}

	/** Field rules copied from the registerContent.zul textbox constraints. */
	public record RegisterRequest(
			@NotBlank @Email(regexp = ".+@.+\\..+", message = "Please enter a valid email address.") String email,
			@NotBlank @Size(min = 1, max = 25) String lastName,
			@NotBlank @Size(min = 1, max = 25) String firstName,
			@Size(max = 500) String institution,
			@Size(max = 255) String sector,
			@Size(max = 500) String purpose,
			@NotBlank @Size(min = 6, max = 100, message = "Password must be 6 to 100 characters.") String password,
			@NotBlank String confirmPassword,
			boolean agreedToTerms,
			String captcha) {}

	public record ForgotRequest(@NotBlank @Email(regexp = ".+@.+\\..+") String email, String captcha) {}

	public record ResetRequest(@NotBlank String token,
			@NotBlank @Size(min = 6, max = 100, message = "Password must be 6 to 100 characters.") String password,
			@NotBlank String confirmPassword) {}

	public record MessageResponse(boolean success, String message) {}

	public record PublicConfig(boolean recaptchaEnabled, String recaptchaSiteKey, String mapsApiKey) {}
}
