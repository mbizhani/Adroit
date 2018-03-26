package org.devocative.adroit;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.KeySpec;

public class KeyTool {
	public enum EKeyLength {
		L128(128), L192(192), L256(256);
		private int len;

		EKeyLength(int len) {
			this.len = len;
		}

		public int getLen() {
			return len;
		}
	}

	private static int ITERATION_COUNT = 65536;

	public static void setIterationCount(int iterationCount) {
		ITERATION_COUNT = iterationCount;
	}

	// ---------------

	public static void generatedKeyStoreWithSecureKey(File keyStoreFile, String keyStorePass, String key, String entryName, String entryProtectionParam) {
		generatedKeyStoreWithSecureKey(keyStoreFile, keyStorePass, key, entryName, entryProtectionParam, EKeyLength.L256);
	}

	public static void generatedKeyStoreWithSecureKey(File keyStoreFile, String keyStorePass, String key, String entryName, String entryProtectionParam, EKeyLength len) {

		try {
			FileOutputStream stream = new FileOutputStream(keyStoreFile, false);

			SecureRandom sr = new SecureRandom();

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

			//char[] password, byte[] salt, int iterationCount, int keyLength
			KeySpec spec = new PBEKeySpec(key.toCharArray(), sr.generateSeed(8), ITERATION_COUNT, len.getLen());

			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(null, keyStorePass.toCharArray());
			ks.setEntry(entryName, new KeyStore.SecretKeyEntry(secret), new KeyStore.PasswordProtection(entryProtectionParam.toCharArray()));
			ks.store(stream, keyStorePass.toCharArray());
			stream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void list() {
		for (Provider provider : Security.getProviders()) {
			System.out.println(provider.getName());
		}
		System.out.println("=====================================");
		System.out.println("=====================================");
		System.out.println("=====================================");
		for (Provider provider : Security.getProviders()) {
			System.out.println(provider.getName());
			for (String key : provider.stringPropertyNames()) {
				System.out.println("\t" + key + "\t" + provider.getProperty(key));
			}
			System.out.println("-------------------------");
		}
	}

	public static void main(String[] args) {
		list();
	}
}
