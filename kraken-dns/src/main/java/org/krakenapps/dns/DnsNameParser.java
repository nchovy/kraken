package org.krakenapps.dns;

import java.nio.ByteBuffer;

public class DnsNameParser {
	private char[] dnsNamePackets = new char[100];

	public int calcDomainLength(ByteBuffer buffer) {
		buffer.mark();
		byte b;
		int domainLength = 0;
		while ((b = buffer.get()) != 0) {
			if (b == -64) {
				buffer.reset();
				return domainLength + 2;
			}
			domainLength++;
		}
		buffer.reset();
		return domainLength + 1; // include 00 bytes
	}

	@SuppressWarnings("static-access")
	public String parse(byte[] replyBuffer, ByteBuffer buffer, int dataLength) {
		byte[] nameServerByteArray = new byte[dataLength];
		@SuppressWarnings("unused")
		ByteBuffer nameServerString = buffer.get(nameServerByteArray, buffer
				.arrayOffset(), dataLength);
		String dnsName = "";
		int pointerIndex = 0;
		pointerIndex = (0xFF & nameServerByteArray[nameServerByteArray.length - 1]);
		storeBytesToPacketArray(nameServerByteArray, replyBuffer, 0,
				pointerIndex);
		dnsName = dnsName.copyValueOf(dnsNamePackets).replaceFirst(".", "");
		return dnsName;
	}

	private byte[] retByteArray(byte[] replyBuffer, int pointerIndex) {
		// This method uses arguments
		// (replyBuffer:Full buffer of reply packet, pointerIndex:Return point
		// in byte array).
		// role : method return to <<additional>> byte array.
		byte[] tempBuffer = new byte[100];
		int i = 0;
		while (replyBuffer[pointerIndex] != 0) {
			tempBuffer[i] = replyBuffer[pointerIndex];
			if (tempBuffer[i] == -64) {
				byte[] addDnsName = new byte[i + 2];
				tempBuffer[i + 1] = replyBuffer[pointerIndex + 1];
				for (int j = 0; j < addDnsName.length; j++)
					addDnsName[j] = tempBuffer[j];
				return addDnsName;
			}
			i++;
			pointerIndex++;
		}

		byte[] addDnsName = new byte[i + 1];
		for (int j = 0; j < addDnsName.length; j++)
			addDnsName[j] = tempBuffer[j];

		return addDnsName;

	}

	private void storeBytesToPacketArray(byte[] insertBytes,
			byte[] replyBuffer, int dnsNamePacketsIndex, int pointerIndex) {
		int strCount = 0;
		int i = 0;
		int insertBytesIndex = 0;
		for (i = dnsNamePacketsIndex; i < dnsNamePacketsIndex
				+ insertBytes.length; i++) {
			if (strCount == 0) {
				strCount = (int) insertBytes[insertBytesIndex];
				dnsNamePackets[i] = '.';
				if (strCount == -64) { // Case : pointer byte.
					// 1. Use retByteArray method, return to <<additional>>
					// byte array.
					// 2. Insert to <<additional>> byte array argument in
					// storeBytesToPacketArray method.
					// 3. As a result, store to <<additional>> byte array in
					// dnsNamePackets.
					byte[] addDomainName = retByteArray(replyBuffer,
							pointerIndex);
					storeBytesToPacketArray(addDomainName, replyBuffer, i,
							(int) addDomainName[addDomainName.length - 1]);
					break;
				}
				insertBytesIndex++;
			} else {
				int charCode = (int) insertBytes[insertBytesIndex];
				dnsNamePackets[i] = (char) charCode;
				strCount--;
				insertBytesIndex++;
			}
		}
		if (dnsNamePackets[i - 1] == '.')
			// If dnsNamePacket doesn't exist pointer packet,
			// dnsNamePacket include <<00>> packet.
			// Therefore, remove this packet.
			dnsNamePackets[i - 1] = 0;
	}
}
