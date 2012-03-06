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
package org.krakenapps.radius.client.auth;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.krakenapps.radius.client.MalformedResponseException;
import org.krakenapps.radius.client.RadiusClient;
import org.krakenapps.radius.protocol.AccessRequest;
import org.krakenapps.radius.protocol.ChapPasswordAttribute;
import org.krakenapps.radius.protocol.RadiusPacket;
import org.krakenapps.radius.protocol.RadiusResponse;
import org.krakenapps.radius.protocol.UserNameAttribute;

public class ChapAuthenticator implements Authenticator {
	private RadiusClient client;
	private String userName;
	private String password;

	public ChapAuthenticator(RadiusClient client, String userName, String password) {
		this.client = client;
		this.userName = userName;
		this.password = password;
	}

	@Override
	public RadiusResponse authenticate() throws IOException {
		String sharedSecret = client.getSharedSecret();
		byte chapIdent = (byte) 0xd2;
		byte[] h = null;

		AccessRequest req = new AccessRequest();

		// calculate hash = MD5(chap ident + password + nonce)
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(chapIdent);
			md5.update(password.getBytes());
			md5.update(req.getAuthenticator());
			h = md5.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}

		// build chap request
		req.setIdentifier(client.getNextId());
		req.setUserName(new UserNameAttribute(userName));
		req.setChapPassword(new ChapPasswordAttribute(chapIdent, h));
		req.finalize();

		// send request
		DatagramSocket socket = new DatagramSocket();
		try {
			socket.connect(client.getIpAddress(), client.getPort());

			byte[] payload = req.getBytes();
			DatagramPacket packet = new DatagramPacket(payload, payload.length);
			socket.setSoTimeout(5000);
			socket.send(packet);

			// receive chap response
			byte[] buf = new byte[65535];
			DatagramPacket response = new DatagramPacket(buf, buf.length);
			socket.receive(response);

			RadiusResponse resp = (RadiusResponse) RadiusPacket.parse(sharedSecret, buf);

			byte[] expectedAuthenticator = RadiusResponse.calculateResponseAuthenticator(resp, sharedSecret,
					req.getAuthenticator());
			if (!Arrays.equals(expectedAuthenticator, resp.getAuthenticator()))
				throw new MalformedResponseException(req, resp);

			return resp;
		} finally {
			socket.close();
		}
	}
}
