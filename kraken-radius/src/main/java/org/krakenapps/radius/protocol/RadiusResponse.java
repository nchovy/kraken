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

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RadiusResponse extends RadiusPacket {
	private RadiusPacket request;
	private String sharedSecret;

	public RadiusResponse() {
	}

	public RadiusResponse(RadiusPacket request, String sharedSecret) {
		this.request = request;
		this.sharedSecret = sharedSecret;
	}

	public RadiusPacket getRequest() {
		return request;
	}

	public void setRequest(RadiusPacket request) {
		this.request = request;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	@Override
	public void finalize() {
		setIdentifier(request.getIdentifier());
		super.finalize();
		finalized = false;

		setAuthenticator(calculateResponseAuthenticator(this, sharedSecret, request.getAuthenticator()));
		finalized = true;
	}

	public static byte[] calculateResponseAuthenticator(RadiusResponse p, String sharedSecret,
			byte[] requestAuthenticator) {
		try {
			ByteBuffer bb = ByteBuffer.allocate(p.getLength() + sharedSecret.getBytes().length);
			bb.put((byte) p.getCode());
			bb.put((byte) p.getIdentifier());
			bb.putShort((short) p.getLength());
			bb.put(requestAuthenticator);

			for (RadiusAttribute attr : p.getAttributes())
				bb.put(attr.getBytes());

			bb.put(sharedSecret.getBytes());
			bb.flip();

			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(bb);
			return md5.digest();
		} catch (NoSuchAlgorithmException e) {
			// should not reachable
			return null;
		}
	}
}
