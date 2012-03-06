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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerGetProcessor {
	private Logger logger = LoggerFactory.getLogger(ServerGetProcessor.class.getName());
	private static final int MAXIMUM_TFTP_PAYLOAD_SIZE = 512;
	private static final int TIMEOUT = 60 * 1000; // 1min timeout

	private DatagramSocket socket;
	private InetAddress clientIp;
	private int clientPort;

	private FileInputStream is;

	private int blockNum = 0;
	private int expectedAckNum = 1;

	private boolean isFileNotFound = false;
	private boolean is512 = false;

	public ServerGetProcessor(DatagramPacket p, String fileName) {
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(TIMEOUT);

			clientIp = p.getAddress();
			clientPort = p.getPort();

			is = new FileInputStream(new File(fileName));
		} catch (SocketException e) {
			logger.warn("kraken tftp: cannot handle get request", e);
		} catch (FileNotFoundException e) {
			logger.warn("kraken tftp: requested file not found", e);
			handleFileNotFound();
			isFileNotFound = true;
		}
	}

	public void start() {
		try {
			if (isFileNotFound)
				return;

			byte[] data;
			do {
				blockNum++;

				if (blockNum > 65535) {
					handleExceed();
					break;
				}

				data = new byte[MAXIMUM_TFTP_PAYLOAD_SIZE];
				int remain = is.available();

				/* send & receive */
				if (remain == 0) {
					if (!is512)
						break;

					send(null);
					receive();
					break;
				} else if (remain < 512) {
					byte[] finalData = new byte[remain];
					sendAndReceive(finalData);
				} else if (remain == 512) {
					is512 = true;
					sendAndReceive(data);
				} else {
					sendAndReceive(data);
				}
			} while (true);
		} catch (IOException e) {
			logger.warn("kraken tftp: cannot handle get request, stopped", e);
		}
	}

	private void sendAndReceive(byte[] data) throws IOException {
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
		byte[] b;
		if (data == null)
			b = setData(blockNum);
		else
			b = setData(blockNum, data);

		DatagramPacket outgoing = new DatagramPacket(b, b.length, clientIp, clientPort);
		socket.send(outgoing);
	}

	private int receive() throws IOException {
		try {
			byte[] inbuf = new byte[200];
			DatagramPacket incoming = new DatagramPacket(inbuf, inbuf.length);
			socket.receive(incoming);

			byte[] tftpData = incoming.getData();

			return (int) (((int) 0 << 24) | ((int) 0 << 16) | ((tftpData[2] << 8) & 0xFFFF) | (tftpData[3] & 0xFF));
		} catch (SocketTimeoutException e) {
			// try next time
			return -1;
		}
	}

	private byte[] setData(int blockNum, byte[] b) {
		ByteBuffer bb = ByteBuffer.allocate(b.length + 4);
		bb.putShort((short) 3);
		bb.putShort((short) blockNum);
		bb.put(b);

		bb.flip();

		int length = bb.limit();
		byte[] b1 = new byte[length];
		bb.get(b1);
		return b1;
	}

	private byte[] setData(int blockNum) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putShort((short) 3);
		bb.putShort((short) blockNum);
		bb.flip();

		byte[] b1 = new byte[4];
		bb.get(b1);
		return b1;
	}

	private void handleFileNotFound() {
		try {
			/* generate tftp error packet(File not found) */
			byte[] b = new byte[] { 0x00, 0x05, 0x00, 0x01, 0x46, 0x69, 0x6c, 0x65, 0x20, 0x6e, 0x6f, 0x74, 0x20, 0x66,
					0x6f, 0x75, 0x6e, 0x64, 0x00 };
			DatagramPacket outgoing = new DatagramPacket(b, b.length, clientIp, clientPort);
			socket.send(outgoing);
		} catch (IOException e) {
			logger.warn("kraken tftp: cannot send file not found error", e);
		}
	}

	private void handleExceed() {
		try {
			/* generate tftp error packet(File size limit exceed) */
			byte[] b = new byte[] { 0x00, 0x05, 0x00, 0x03, 0x46, 0x69, 0x6c, 0x65, 0x20, 0x73, 0x69, 0x7a, 0x65, 0x20,
					0x6c, 0x69, 0x6d, 0x69, 0x74, 0x20, 0x65, 0x78, 0x63, 0x65, 0x65, 0x64, 0x00 };
			DatagramPacket outgoing = new DatagramPacket(b, b.length, clientIp, clientPort);
			socket.send(outgoing);
		} catch (IOException e) {
			logger.warn("kraken tftp: cannot send file size limit exceeded error", e);
		}
	}
}