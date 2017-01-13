package org.devocative.adroit;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class KeyTool {
	public static void generatedKeyStoreWithSecureKey(File keyStoreFile, String keyStorePass, String key, String entryName, String entryProtectionParam) {

		try {
			FileOutputStream stream = new FileOutputStream(keyStoreFile, false);

			SecureRandom sr = new SecureRandom();

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(key.toCharArray(), sr.generateSeed(8), 65536, 256);
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
}
