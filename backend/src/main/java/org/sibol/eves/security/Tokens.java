package org.sibol.eves.security;

import java.security.SecureRandom;

/**
 * Random tokens stored in model.User fields. Legacy formats are kept so links sent by the old app still work:
 * e-mail verification = 50 letters, password reset = 70 letters, e-mail-config invitation = 50 letters.
 * New reset tokens carry a "reset:" prefix so a pending reset no longer blocks login (legacy bug).
 */
public final class Tokens {
	public static final String RESET_PREFIX = "reset:";
	/** Marker written to User.improvement/inquiry/bugreport/suggestastudy once an invitation is accepted. */
	public static final String ACCEPTED = "accepted_invi";

	private static final SecureRandom RND = new SecureRandom();
	private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private Tokens() {}

	public static String random(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
			sb.append(LETTERS.charAt(RND.nextInt(LETTERS.length())));
		return sb.toString();
	}

	public static String newVerification() { return random(50); }
	public static String newReset() { return RESET_PREFIX + random(70); }
	public static String newInvitation() { return random(50); }

	/** A pending password reset (new prefixed form or the legacy 70-letter form). */
	public static boolean isReset(String validation) {
		return validation != null && (validation.startsWith(RESET_PREFIX) || validation.length() == 70);
	}

	/** Login is blocked only while e-mail verification is pending (not while a reset is pending). */
	public static boolean blocksLogin(String validation) {
		return validation != null && !validation.isEmpty() && !isReset(validation);
	}
}
