package org.devocative.adroit;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringEncryptorUtil {
	private static boolean bypassSecurity = false;

	public static void setBypassSecurity(boolean bypassSecurity) {
		StringEncryptorUtil.bypassSecurity = bypassSecurity;
	}

	public static String hash(String str) {
		if (bypassSecurity) {
			return str;
		}

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes(StandardCharsets.UTF_8));
			return Base64.encodeBase64String(md.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("StringEncryptorUtil.hash: ", e);
		}
	}

	public static String decrypt(String str) {
		return str;
	}

	public static String encrypt(String str) {
		return str;
	}
}
