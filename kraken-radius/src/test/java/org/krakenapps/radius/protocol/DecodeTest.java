package org.krakenapps.radius.protocol;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import static org.junit.Assert.*;

public class DecodeTest {
	@Test
	public void decodeUserPasswordTest() {
		byte[] authenticator = { 0x34, 0x65, (byte) 0xa0, (byte) 0x8e, (byte) 0x9e, 0x7a, (byte) 0x9c, (byte) 0xb1,
				0x50, 0x42, 0x25, 0x28, (byte) 0xb0, (byte) 0x83, 0x40, (byte) 0x86 };

		byte[] b = new byte[] { 0x02, 0x12, 0x26, 0x4c, (byte) 0x9e, (byte) 0xae, 0x77, (byte) 0xbe, 0x28, 0x41,
				(byte) 0xd9, (byte) 0xa0, 0x6f, 0x60, 0x49, (byte) 0xc3, (byte) 0xc0, 0x13 };

		UserPasswordAttribute p = new UserPasswordAttribute(authenticator, "10testing", b, 0, b.length);
		assertEquals("radiustest", p.getPassword());
	}

	@Test
	public void decodeNasIpAddressTest() throws UnknownHostException {
		byte[] b = new byte[] { 0x04, 0x06, 0x7f, 0x00, 0x00, 0x01 };
		NasIpAddressAttribute attr = new NasIpAddressAttribute(b, 0, b.length);
		assertEquals(InetAddress.getByName("127.0.0.1"), attr.getAddress());
	}

	@Test
	public void decodeNasPortTest() {
		byte[] b = new byte[] { 0x05, 0x06, 0x00, 0x00, 0x00, 0x00 };
		NasPortAttribute attr = new NasPortAttribute(b, 0, b.length);
		assertEquals(0, attr.getPort());
	}
}
