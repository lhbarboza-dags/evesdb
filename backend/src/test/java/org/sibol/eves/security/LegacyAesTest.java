package org.sibol.eves.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.sibol.eves.config.AppProperties;

/** The new cipher must produce exactly what org.sibol.eves.app.AES256 stored in the user table. */
class LegacyAesTest {

	private static final String SECRET = "test_secret_key";
	private static final String SALT = "test_salt_value!";

	/** Verbatim algorithm of the legacy AES256.encrypt (constants injected). */
	static String legacyEncrypt(String s) throws Exception {
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(SECRET.toCharArray(), SALT.getBytes(), 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
		return Base64.getEncoder().encodeToString(cipher.doFinal(s.getBytes(StandardCharsets.UTF_8)));
	}

	static AppProperties props(boolean upgrade) {
		return new AppProperties("http://x", new AppProperties.Mail("a@b.c", false),
				new AppProperties.Recaptcha(false, "", ""), new AppProperties.Maps("", 0),
				new AppProperties.LegacyAes(SECRET, SALT), new AppProperties.Auth(upgrade));
	}

	@Test
	void encryptMatchesLegacy() throws Exception {
		LegacyAes aes = new LegacyAes(props(false));
		assertThat(aes.encrypt("p@ssw0rd!")).isEqualTo(legacyEncrypt("p@ssw0rd!"));
		assertThat(aes.decrypt(legacyEncrypt("héllo"))).isEqualTo("héllo");
		assertThat(aes.decrypt("not-base64!!")).isNull();
	}

	@Test
	void passwordServiceAcceptsLegacyAndBcrypt() throws Exception {
		PasswordService legacyMode = new PasswordService(new LegacyAes(props(false)), props(false));
		String stored = legacyEncrypt("secret1");
		assertThat(legacyMode.matches("secret1", stored)).isTrue();
		assertThat(legacyMode.matches("wrong", stored)).isFalse();
		assertThat(legacyMode.needsUpgrade(stored)).isFalse();
		assertThat(legacyMode.encode("secret1")).isEqualTo(stored);

		PasswordService upgradeMode = new PasswordService(new LegacyAes(props(true)), props(true));
		assertThat(upgradeMode.needsUpgrade(stored)).isTrue();
		String bcrypt = upgradeMode.encode("secret1");
		assertThat(bcrypt).startsWith("$2");
		assertThat(upgradeMode.matches("secret1", bcrypt)).isTrue();
		assertThat(legacyMode.matches("secret1", bcrypt)).isTrue();
	}

	@Test
	void tokens() {
		assertThat(Tokens.newVerification()).hasSize(50);
		assertThat(Tokens.isReset(Tokens.newReset())).isTrue();
		assertThat(Tokens.isReset("a".repeat(70))).isTrue();
		assertThat(Tokens.blocksLogin(Tokens.newVerification())).isTrue();
		assertThat(Tokens.blocksLogin(Tokens.newReset())).isFalse();
		assertThat(Tokens.blocksLogin("")).isFalse();
		assertThat(Tokens.blocksLogin(null)).isFalse();
	}
}
