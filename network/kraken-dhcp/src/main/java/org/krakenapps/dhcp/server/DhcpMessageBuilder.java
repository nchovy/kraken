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
package org.krakenapps.dhcp.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import org.krakenapps.dhcp.DhcpMessage;
import org.krakenapps.dhcp.DhcpOption;
import org.krakenapps.dhcp.DhcpOptionCode;
import org.krakenapps.dhcp.model.DhcpOptionConfig;
import org.krakenapps.dhcp.options.ByteConverter;
import org.krakenapps.dhcp.options.DhcpMessageTypeOption;
import org.krakenapps.dhcp.options.DhcpServerIdentifierOption;
import org.krakenapps.dhcp.options.RawDhcpOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DhcpMessageBuilder {
	private static InetAddress ZERO_ADDRESS;

	static {
		try {
			ZERO_ADDRESS = InetAddress.getByName("0.0.0.0");
		} catch (UnknownHostException e) {
			// not reachable
		}
	}

	public static DhcpMessage newOffer(DhcpMessage msg, List<DhcpOptionConfig> configs, InetAddress yourAddress) {
		InetAddress serverIp = DhcpDatabase.getServerIdentifier(configs);

		DhcpMessage m = new DhcpMessage();
		m.setMessageType(2); // boot reply
		m.setHardwareType(1); // ethernet
		m.setHardwareAddressLength(6);
		m.setHops(0);
		m.setTransactionId(msg.getTransactionId());
		m.setSecs(0);
		m.setFlags(msg.getFlags());
		m.setClientAddress(ZERO_ADDRESS);
		m.setYourAddress(yourAddress);
		m.setNextServerAddress(serverIp);
		m.setGatewayAddress(msg.getGatewayAddress());
		m.setClientMac(msg.getClientMac());
		m.setServerName(null);
		m.setBootFileName(null);

		List<DhcpOption> options = m.getOptions();
		options.add(new DhcpMessageTypeOption(DhcpMessage.Type.Offer));
		addOptions(configs, options);
		return m;
	}

	public static DhcpMessage newAck(DhcpMessage msg, List<DhcpOptionConfig> configs, InetAddress yourAddress) {
		InetAddress serverIp = DhcpDatabase.getServerIdentifier(configs);

		DhcpMessage m = new DhcpMessage();
		m.setMessageType(2); // boot reply
		m.setHardwareType(1); // ethernet
		m.setHardwareAddressLength(6);
		m.setHops(0);
		m.setTransactionId(msg.getTransactionId());
		m.setSecs(0);
		m.setFlags(msg.getFlags());
		m.setClientAddress(ZERO_ADDRESS);
		m.setYourAddress(yourAddress);
		m.setNextServerAddress(serverIp);
		m.setGatewayAddress(msg.getGatewayAddress());
		m.setClientMac(msg.getClientMac());
		m.setServerName(null);
		m.setBootFileName(null);

		// 53, 1, 58, 59, 51, 54, 15, 3, 6
		List<DhcpOption> options = m.getOptions();
		options.add(new DhcpMessageTypeOption(DhcpMessage.Type.Ack));
		addOptions(configs, options);

		return m;
	}

	private static void addOptions(List<DhcpOptionConfig> configs, List<DhcpOption> options) {
		for (DhcpOptionConfig config : configs) {
			byte[] value = encode(config);
			if (value != null)
				options.add(new RawDhcpOption(config.getType(), value.length, value));
			else
				warn("kraken dhcp: cannot encode {}, {}", config.getType(), config.getValue());
		}
	}

	public static DhcpMessage newNak(DhcpMessage msg, InetAddress serverIp) {
		DhcpMessage m = new DhcpMessage();
		m.setMessageType(2); // boot reply
		m.setHardwareType(1); // ethernet
		m.setHardwareAddressLength(6);
		m.setHops(0);
		m.setTransactionId(msg.getTransactionId());
		m.setSecs(0);
		m.setFlags(msg.getFlags());
		m.setClientAddress(ZERO_ADDRESS);
		m.setYourAddress(ZERO_ADDRESS);
		m.setNextServerAddress(ZERO_ADDRESS);
		m.setGatewayAddress(ZERO_ADDRESS);
		m.setClientMac(msg.getClientMac());
		m.setServerName(null);
		m.setBootFileName(null);

		List<DhcpOption> options = m.getOptions();
		options.add(new DhcpMessageTypeOption(DhcpMessage.Type.Nak));
		options.add(new DhcpServerIdentifierOption(serverIp));

		return m;
	}

	private static byte[] encode(DhcpOptionConfig c) {
		int type = c.getType();
		DhcpOptionCode code = DhcpOptionCode.from(type);
		if (code == null)
			return null;

		Class<?> clazz = code.getValueType();
		if (clazz == null)
			return null;

		try {
			if (clazz.equals(Boolean.class))
				return new byte[] { (byte) (Boolean.parseBoolean(c.getValue()) ? 1 : 0) };
			if (clazz.equals(Byte.class))
				return new byte[] { Byte.valueOf(c.getValue()) };
			if (clazz.equals(String.class))
				return c.getValue().getBytes(Charset.forName("utf-8"));
			if (clazz.equals(Integer.class))
				return ByteConverter.convert(Integer.valueOf(c.getValue()));
			if (clazz.equals(Short.class))
				return ByteConverter.convert(Short.valueOf(c.getValue()));
			if (clazz.equals(InetAddress.class))
				return InetAddress.getByName(c.getValue()).getAddress();
			if (clazz.equals(Short[].class)) {
				String[] tokens = c.getValue().split(",");
				byte[] b = new byte[tokens.length * 2];
				ByteBuffer bb = ByteBuffer.wrap(b);

				for (String token : tokens)
					bb.put(ByteConverter.convert(Short.valueOf(token)));

				return b;
			}
			if (clazz.equals(InetAddress[].class)) {
				String[] tokens = c.getValue().split(",");
				byte[] b = new byte[tokens.length * 4];
				ByteBuffer bb = ByteBuffer.wrap(b);

				for (String token : tokens)
					bb.put(InetAddress.getByName(token).getAddress());

				return b;
			}
		} catch (Exception e) {
		}

		return null;
	}

	public static byte[] encode(DhcpMessage msg) {
		int optionLength = 0;

		for (DhcpOption o : msg.getOptions())
			optionLength += (2 + o.getLength());

		byte[] b = new byte[241 + optionLength];

		ByteBuffer bb = ByteBuffer.wrap(b);
		// 28 byte
		bb.put(msg.getMessageType());
		bb.put(msg.getHardwareType());
		bb.put(msg.getHardwareAddressLength());
		bb.put(msg.getHops());
		bb.putInt(msg.getTransactionId());
		bb.putShort(msg.getSecs());
		bb.putShort(msg.getFlags());
		bb.put(msg.getClientAddress().getAddress());
		bb.put(msg.getYourAddress().getAddress());
		bb.put(msg.getNextServerAddress().getAddress());
		bb.put(msg.getGatewayAddress().getAddress());

		byte[] chaddr = new byte[16];
		byte[] mac = msg.getClientMac().getBytes();
		for (int i = 0; i < 6; i++)
			chaddr[i] = mac[i];

		// 212 bytes
		bb.put(chaddr);
		bb.put(new byte[64]);
		bb.put(new byte[128]);
		bb.put(new byte[] { 0x63, (byte) 0x82, 0x53, 0x63 });

		for (DhcpOption o : msg.getOptions()) {
			bb.put(o.getType());
			bb.put((byte) o.getLength());
			bb.put(o.getValue());
		}

		// end option
		bb.put((byte) 0xff);

		return b;
	}

	private static void warn(String msg, Object... args) {
		Logger logger = LoggerFactory.getLogger(DhcpMessageBuilder.class.getName());
		logger.warn(msg, args);
	}
}
