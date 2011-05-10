package org.krakenapps.radius.client.auth;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.krakenapps.radius.protocol.AccessRequest;
import org.krakenapps.radius.protocol.NasIpAddressAttribute;
import org.krakenapps.radius.protocol.NasPortAttribute;
import org.krakenapps.radius.protocol.RadiusPacket;
import org.krakenapps.radius.protocol.UserNameAttribute;
import org.krakenapps.radius.protocol.UserPasswordAttribute;

public class PapAuthenticator implements Authenticator {

	private String userName;
	private String password;

	public PapAuthenticator(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	@Override
	public RadiusPacket authenticate(InetAddress addr, int port, String sharedSecret) throws IOException {
		AccessRequest req = new AccessRequest();
		req.setUserName(new UserNameAttribute(userName));
		req.setUserPassword(new UserPasswordAttribute(req.getAuthenticator(), sharedSecret, password));
		req.setNasIpAddress(new NasIpAddressAttribute(InetAddress.getByName("127.0.0.1")));
		req.setNasPort(new NasPortAttribute(0));

		byte[] payload = req.getBytes();
		DatagramSocket socket = new DatagramSocket(port, addr);
		DatagramPacket packet = new DatagramPacket(payload, payload.length);
		socket.bind(null);
		socket.setSoTimeout(5000);
		socket.send(packet);

		byte[] buf = new byte[65535];
		DatagramPacket response = new DatagramPacket(buf, buf.length);
		socket.receive(response);
		
		// TODO: not completed yet
		return null;
	}

}
