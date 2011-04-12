package org.krakenapps.filemon.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
	private static final String[] hexTable = new String[256];

	static {
		buildHexTable();
	}

	private HashUtils() {
	}

	private static void buildHexTable() {
		int p = 0;

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++)
				hexTable[p++] = "" + i + j;

			for (int j = 0; j < 6; j++)
				hexTable[p++] = "" + i + (char) ('a' + j);
		}

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 10; j++)
				hexTable[p++] = "" + (char) ('a' + i) + j;

			for (int j = 0; j < 6; j++)
				hexTable[p++] = "" + ((char) ('a' + i)) + (char) ('a' + j);
		}
	}

	public static String getMd5(File f) throws IOException {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			return digest(md5, f);
		} catch (NoSuchAlgorithmException e) {
			// should not reachable
			return null;
		}
	}

	public static String getSha1(File f) throws IOException {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			return digest(sha1, f);
		} catch (NoSuchAlgorithmException e) {
			// should not reachable
			return null;
		}
	}

	public static String digest(MessageDigest method, File f) throws NoSuchAlgorithmException, FileNotFoundException,
			IOException {

		byte[] b = new byte[4096];
		FileInputStream fs = new FileInputStream(f);
		while (true) {
			int readBytes = fs.read(b);
			if (readBytes < 0)
				break;

			method.update(b, 0, readBytes);
		}

		byte[] digest = method.digest();
		return toHexString(digest);
	}

	private static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++)
			sb.append(hexTable[b[i] & 0xff]);
		return sb.toString();
	}

}
