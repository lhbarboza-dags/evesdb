package org.sibol.eves.security;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.sibol.eves.config.AppProperties;
import org.springframework.stereotype.Component;

/**
 * Byte-for-byte compatible port of the legacy org.sibol.eves.app.AES256 password cipher
 * (PBKDF2WithHmacSHA256, 65536 rounds, AES/CBC/PKCS5Padding, zero IV). The key material now comes from configuration.
 */
@Component
public class LegacyAes {

	private final SecretKeySpec key;

	public LegacyAes(AppProperties props) {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(props.legacyAes().secret().toCharArray(),
					props.legacyAes().salt().getBytes(), 65536, 256);
			this.key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		} catch (Exception e) {
			throw new IllegalStateException("Cannot initialise legacy AES key", e);
		}
	}

	public String encrypt(String plain) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));
			return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/** @return the plain text, or null when the value is not a legacy ciphertext. */
	public String decrypt(String cipherText) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
			return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
		} catch (Exception e) {
			return null;
		}
	}
}
