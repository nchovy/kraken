package org.krakenapps.rpc.impl;

import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordUtil {
	private PasswordUtil() {
	}

	public static String calculatePasswordHash(String password) {
		return SHA1(password);
	}

	public static String calculatePasswordHash(String password, String nonce) {
		return SHA1(password + nonce);
	}

	private static String SHA1(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			sha1hash = md.digest();
			return convertToHex(sha1hash);
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(PasswordUtil.class.getName());
			logger.warn("kraken-rpc: SHA1 hash failed", e);
			return null;
		}
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

}
