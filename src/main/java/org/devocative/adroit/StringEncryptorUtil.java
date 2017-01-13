package org.devocative.adroit;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class StringEncryptorUtil {
	private static boolean bypassSecurity = false;
	private static Cipher deCipher, enCipher;

	// ------------------------------

	public static void init(InputStream keyStoreStream, String keyStorePass, String entryName, String entryProtectionParam) {
		if (bypassSecurity) {
			return;
		}

		try {
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(keyStoreStream, keyStorePass.toCharArray());

			KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(entryName,
				new KeyStore.PasswordProtection(entryProtectionParam.toCharArray()));
			SecretKey key = entry.getSecretKey();

			enCipher = Cipher.getInstance(key.getAlgorithm());
			enCipher.init(Cipher.ENCRYPT_MODE, key);

			deCipher = Cipher.getInstance(key.getAlgorithm());
			deCipher.init(Cipher.DECRYPT_MODE, key);

			keyStoreStream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void setBypassSecurity(boolean bypassSecurity) {
		StringEncryptorUtil.bypassSecurity = bypassSecurity;
	}

	public static String encodeBase64(byte[] b) {
		return Base64.encodeBase64String(b);
	}

	public static String decodeBase64(String str) {
		return new String(decodeBASE64(str), StandardCharsets.UTF_8);
	}

	public static String hash(String str) {
		if (bypassSecurity) {
			return str;
		}

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes(StandardCharsets.UTF_8));
			return encodeBase64(md.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("StringEncryptorUtil.hash: ", e);
		}
	}

	public static String decrypt(String str) {
		if (bypassSecurity) {
			return str;
		}

		if (str == null) {
			throw new RuntimeException("Invalid parameter: null");
		}

		if (deCipher == null) {
			throw new RuntimeException("No Decryption Cipher: Bad StringEncryptorUtil init");
		}

		try {
			byte[] dec = decodeBASE64(str);
			byte[] utf8 = deCipher.doFinal(dec);
			return new String(utf8, "UTF8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String encrypt(String str) {
		if (bypassSecurity) {
			return str;
		}

		if (str == null) {
			throw new RuntimeException("Invalid parameter: null");
		}

		if (enCipher == null) {
			throw new RuntimeException("No Encryption Cipher: Bad StringEncryptorUtil init");
		}

		try {
			byte[] utf8 = str.getBytes("UTF8");
			byte[] enc = enCipher.doFinal(utf8);
			return encodeBase64(enc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ------------------------------

	private static byte[] decodeBASE64(String str) {
		return Base64.decodeBase64(str);
	}
}
