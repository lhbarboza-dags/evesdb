package org.sibol.eves.security;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import org.sibol.eves.config.AppProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Verifies both legacy AES-encrypted passwords and bcrypt hashes.
 * New passwords are stored in the legacy AES format while app.auth.upgrade-hash=false, so the old ZK app
 * (which only understands AES) keeps working against the same database. Switch the flag on once ZK is retired.
 */
@Service
public class PasswordService {

	private final LegacyAes aes;
	private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
	private final boolean upgrade;

	public PasswordService(LegacyAes aes, AppProperties props) {
		this.aes = aes;
		this.upgrade = props.auth() != null && props.auth().upgradeHash();
	}

	public boolean matches(String raw, String stored) {
		if (raw == null || stored == null || stored.isEmpty())
			return false;
		if (isBcrypt(stored))
			return bcrypt.matches(raw, stored);
		String plain = aes.decrypt(stored);
		return plain != null && MessageDigest.isEqual(plain.getBytes(StandardCharsets.UTF_8), raw.getBytes(StandardCharsets.UTF_8));
	}

	public String encode(String raw) {
		return upgrade ? bcrypt.encode(raw) : aes.encrypt(raw);
	}

	/** True when a stored legacy value should be re-encoded after a successful login. */
	public boolean needsUpgrade(String stored) {
		return upgrade && !isBcrypt(stored);
	}

	private static boolean isBcrypt(String s) {
		return s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$");
	}
}
