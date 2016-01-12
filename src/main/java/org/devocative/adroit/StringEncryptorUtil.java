package org.devocative.adroit;

public class StringEncryptorUtil {
	private static boolean bypassSecurity = false;

	public static void setBypassSecurity(boolean bypassSecurity) {
		StringEncryptorUtil.bypassSecurity = bypassSecurity;
	}

	public static String decrypt(String str) {
		return str;
	}

	public static String encrypt(String str) {
		return str;
	}
}
