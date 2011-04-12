package org.krakenapps.sonar.passive.safebrowsing.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Hasher {
	public static String generateHash(String src) {
		if (src == null)
			return null;

		StringBuffer hexString = new StringBuffer();

		try {
			MessageDigest mdAlgorithm = MessageDigest.getInstance("MD5");
			mdAlgorithm.update(src.getBytes());
			byte[] digest = mdAlgorithm.digest();
			for (int i = 0; i < digest.length; i++) {
				src = Integer.toHexString(0xFF & digest[i]);
				if (src.length() < 2)
					src = "0" + src;
				hexString.append(src);
			}
		}
		catch(NoSuchAlgorithmException e) {
			System.out.println("Error! MD5 algorithm not found!");
			return null;
		}

		return hexString.toString();
	}
}
