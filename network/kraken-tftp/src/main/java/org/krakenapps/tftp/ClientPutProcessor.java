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
package org.krakenapps.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientPutProcessor {
	private static final int TIMEOUT = 60 * 1000; // 1min timeout
	private static final int MAXIMUM_TFTP_PACKET_SIZE = 512;
	private Logger logger = LoggerFactory.getLogger(ClientPutProcessor.class.getName());

	private DatagramSocket socket;
	private InetAddress target;
	private int port;

	private short blockNum = 0;
	private int expectedAckNum = 1;

	private boolean is512 = false;

	public ClientPutProcessor() {
	}

	public void start(InetSocketAddress target, byte[] firstPacket, String fileName) throws IOException {
		socket = new DatagramSocket();
		socket.setSoTimeout(TIMEOUT);

		sendFirstPacket(target, firstPacket);
		processing(target.getAddress(), fileName);
	}

	private void sendFirstPacket(InetSocketAddress target, byte[] firstPacket) throws IOException {
		DatagramPacket outgoing = new DatagramPacket(firstPacket, firstPacket.length, target.getAddress(), target
				.getPort());
		socket.send(outgoing);
	}

	private void processing(InetAddress target, String fileName) throws IOException {
		FileInputStream is = null;
		try {
			/* first received */
			byte[] inbuf = new byte[200];
			DatagramPacket incoming = new DatagramPacket(inbuf, inbuf.length);
			socket.receive(incoming);

			this.target = target;
			this.port = incoming.getPort();

			is = new FileInputStream(new File(fileName));
			handleSocket(target, port, is);
		} finally {
			if (is != null)
				is.close();
		}
	}

	private void handleSocket(InetAddress target, int port, FileInputStream is) throws IOException {
		byte[] data;

		do {
			blockNum++;
			data = new byte[MAXIMUM_TFTP_PACKET_SIZE];

			int remain = is.available();

			if (remain == 0) {
				if (!is512)
					break;

				send(null);
				receive();
				break;
			} else if (remain < 512) {
				byte[] finalData = new byte[remain];
				sendAndReceive(is, finalData);
			} else if (remain == 512) {
				is512 = true;
				sendAndReceive(is, data);
			} else {
				sendAndReceive(is, data);
			}
		} while (true);
	}

	private void sendAndReceive(FileInputStream is, byte[] data) throws IOException {
		while (true) {
			is.read(data);
			send(data);
			int ackNum = receive();

			if (ackNum == expectedAckNum) {
				expectedAckNum++;
				break;
			}
		}
	}

	private void send(byte[] data) throws IOException {
		try {
			byte[] b;
			if (data == null)
				b = setData(blockNum);
			else
				b = setData(blockNum, data);

			DatagramPacket outgoing = new DatagramPacket(b, b.length, target, port);
			socket.send(outgoing);
		} catch (SocketTimeoutException e) {
			if (logger.isDebugEnabled())
				logger.debug("kraken tftp: socket timeout exception");
		}
	}

	private int receive() throws IOException {
		byte[] inbuf = new byte[200];
		DatagramPacket incoming = new DatagramPacket(inbuf, inbuf.length);
		socket.receive(incoming);

		byte[] tftpData = incoming.getData();
		return (int) (((int) 0 << 24) | ((int) 0 << 16) | ((tftpData[2] << 8) & 0xFFFF) | (tftpData[3] & 0xFF));
	}

	private byte[] setData(short blockNum, byte[] b) {
		ByteBuffer bb = ByteBuffer.allocate(b.length + 4);
		bb.putShort((short) 3);
		bb.putShort(blockNum);
		bb.put(b);

		bb.flip();

		int length = bb.limit();
		byte[] b1 = new byte[length];
		bb.get(b1);
		return b1;
	}

	private byte[] setData(short blockNum) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putShort((short) 3);
		bb.putShort(blockNum);
		bb.flip();

		byte[] b1 = new byte[4];
		bb.get(b1);
		return b1;
	}
}