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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerPutProcessor {
	private static final int MAXIMUM_TFTP_PACKET_SIZE = 512;
	private Logger logger = LoggerFactory.getLogger(ClientGetProcessor.class.getName());

	private DatagramSocket socket;
	private InetAddress clientIp;
	private int clientPort;

	private int ackNum = 0;

	public ServerPutProcessor(DatagramPacket p) {
		try {
			socket = new DatagramSocket();

			clientIp = p.getAddress();
			clientPort = p.getPort();
		} catch (SocketException e) {
			logger.error("kraken tftp: ack socket error", e);
		}
	}

	public void start(String fileName) {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(new File(fileName));
			sendFirstPacket();

			processing(os);
		} catch (IOException e) {
			logger.error("kraken tftp: cannot handle put request, stopped", e);
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
	}

	public void block() {
		try {
			byte[] b = new byte[] { 0x00, 0x05, 0x00, 0x00, 0x50, 0x65, 0x72, 0x6d, 0x69, 0x73, 0x73, 0x69, 0x6f, 0x6e,
					0x20, 0x64, 0x65, 0x6e, 0x69, 0x65, 0x64, 0x00 };
			DatagramPacket outgoing = new DatagramPacket(b, b.length, clientIp, clientPort);
			socket.send(outgoing);
		} catch (IOException e) {
			logger.error("kraken tftp: cannot block", e);
		}
	}

	private void sendFirstPacket() throws IOException {
		byte[] firstPacket = setData(ackNum);
		DatagramPacket outgoing = new DatagramPacket(firstPacket, firstPacket.length, clientIp, clientPort);
		socket.send(outgoing);
	}

	private void processing(FileOutputStream os) throws IOException {
		DatagramPacket incoming;
		DatagramPacket outgoing;
		byte[] inbuf;

		while (true) {
			inbuf = new byte[516];
			incoming = new DatagramPacket(inbuf, inbuf.length);
			socket.receive(incoming);
			os.write(incoming.getData(), 4, incoming.getLength() - 4);

			ackNum++;
			if (ackNum > 65535) {
				sendFileSizeLimitExceededError();
				break;
			}

			byte[] b = setData(ackNum);
			outgoing = new DatagramPacket(b, b.length, clientIp, clientPort);
			socket.send(outgoing);

			// last packet will be smaller than maximum packet size
			if (incoming.getLength() < MAXIMUM_TFTP_PACKET_SIZE)
				break;
		}
	}

	private byte[] setData(int ackNum) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putShort((short) 4);
		bb.putShort((short) ackNum);
		bb.flip();
		
		byte[] b = new byte[4];
		bb.get(b);
		return b;
	}

	private void sendFileSizeLimitExceededError() throws IOException {
		/* generate tftp error packet(File size limit exceed) */
		byte[] b = new byte[] { 0x00, 0x05, 0x00, 0x03, 0x46, 0x69, 0x6c, 0x65, 0x20, 0x73, 0x69, 0x7a, 0x65, 0x20,
				0x6c, 0x69, 0x6d, 0x69, 0x74, 0x20, 0x65, 0x78, 0x63, 0x65, 0x65, 0x64, 0x00 };
		DatagramPacket outgoing = new DatagramPacket(b, b.length, clientIp, clientPort);
		socket.send(outgoing);
	}
}