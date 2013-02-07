/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package org.krakenapps.safebrowsing.util;

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
