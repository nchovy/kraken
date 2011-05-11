/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.radius.protocol;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserPasswordAttribute extends RadiusAttribute {

	private byte[] authenticator;
	private String sharedSecret;
	private String password;

	public UserPasswordAttribute(byte[] authenticator, String sharedSecret, byte[] encoded, int offset, int length) {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not user password attribute");

		this.authenticator = authenticator;
		this.sharedSecret = sharedSecret;

		// copy encoded password
		int len = encoded[offset + 1] - 2;
		byte[] b = new byte[len];
		for (int i = 0; i < len; i++)
			b[i] = encoded[offset + 2 + i];

		// decode
		this.password = decodePassword(b);
	}

	public UserPasswordAttribute(byte[] authenticator, String sharedSecret, String password) {
		this.authenticator = authenticator;
		this.sharedSecret = sharedSecret;
		this.password = password;
	}

	@Override
	public int getType() {
		return 2;
	}

	public byte[] getAuthenticator() {
		return authenticator;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public byte[] getBytes() {
		byte[] encoded = encodePassword();
		return encodeString(getType(), encoded);
	}

	private String decodePassword(byte[] encoded) {
		// b1 = MD5(S + RA) -> c(1) = p1 xor b1
		// b2 = MD5(S + c(1)) -> c(2) = p2 xor b2
		try {
			byte[] plain = new byte[encoded.length];
			int folds = encoded.length / 16;
			byte[] seed = authenticator;

			for (int i = 0; i < folds; i++) {
				// b[n] = md5
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(sharedSecret.getBytes());
				md5.update(seed);
				byte[] b = md5.digest();

				// plain = c[n] xor b[n]
				for (int j = 0; j < 16; j++)
					plain[i * 16 + j] = (byte) (encoded[i * 16 + j] ^ b[j]);

				// copy result
				byte[] c = new byte[16];
				for (int j = 0; j < 16; j++)
					c[j] = encoded[j];

				seed = c;
			}

			return new String(plain).trim();
		} catch (NoSuchAlgorithmException e) {
			// should not reachable
			return null;
		}
	}

	private byte[] encodePassword() {
		// b1 = MD5(S + RA) -> c(1) = p1 xor b1
		// b2 = MD5(S + c(1)) -> c(2) = p2 xor b2
		try {
			byte[] p = expand(password);
			byte[] result = new byte[p.length];
			int folds = p.length / 16;
			byte[] seed = authenticator;

			for (int i = 0; i < folds; i++) {
				// b[n] = md5
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(sharedSecret.getBytes());
				md5.update(seed);
				byte[] b = md5.digest();

				// c[n] = xor
				byte[] c = new byte[16];
				for (int j = 0; j < 16; j++)
					c[j] = (byte) (p[i * 16 + j] ^ b[j]);

				// copy result
				for (int j = 0; j < 16; j++)
					result[i * 16 + j] = c[j];

				seed = c;
			}

			return result;
		} catch (NoSuchAlgorithmException e) {
			// should not reachable
			return null;
		}
	}

	private byte[] expand(String password) {
		if (password == null || password.length() == 0)
			throw new IllegalArgumentException("password should be not null and not empty");

		byte[] p = password.getBytes();
		int expandedLength = (((p.length - 1) / 16) + 1) * 16;
		byte[] expanded = new byte[expandedLength];
		for (int i = 0; i < expanded.length; i++) {
			if (i < p.length)
				expanded[i] = p[i];
			else
				expanded[i] = 0;
		}
		return expanded;
	}
}
