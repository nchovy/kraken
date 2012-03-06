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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGetProcessor {
	private static final int TIMEOUT = 60 * 1000; // 1min timeout
	private static final int MAXIMUM_TFTP_PACKET_SIZE = 512;
	private Logger logger = LoggerFactory.getLogger(ClientGetProcessor.class.getName());

	private DatagramSocket socket;
	private short ackNum = 0;

	public ClientGetProcessor() {
	}

	public void start(InetSocketAddress target, byte[] firstPacket, String source, String destination)
			throws IOException {
		socket = new DatagramSocket();
		socket.setSoTimeout(TIMEOUT);

		sendFirstPacket(target, firstPacket);
		processing(target.getAddress(), source, destination);
	}

	private void sendFirstPacket(InetSocketAddress target, byte[] firstPacket) throws IOException {
		DatagramPacket outgoing = new DatagramPacket(firstPacket, firstPacket.length, target.getAddress(), target
				.getPort());
		socket.send(outgoing);
	}

	private void processing(InetAddress target, String source, String destination) throws IOException {
		FileOutputStream os = null;
		try {
			/* first received */
			byte[] inbuf = new byte[516];
			DatagramPacket incoming = new DatagramPacket(inbuf, inbuf.length);
			socket.receive(incoming);

			if (incoming == null || incoming.getData()[1] == 0x05)
				throw new FileNotFoundException();

			if (destination == null) {
				int pos = source.lastIndexOf("/");
				if (pos == -1)
					os = new FileOutputStream(new File(source));
				else
					os = new FileOutputStream(new File(source.substring(pos + 1)));
			} else
				os = new FileOutputStream(new File(destination));

			os.write(incoming.getData(), 4, incoming.getLength() - 4);
			int port = incoming.getPort();

			/* first ACK */
			ackNum++;
			byte[] b = setData(ackNum);
			DatagramPacket outgoing = new DatagramPacket(b, b.length, target, port);
			socket.send(outgoing);

			if (incoming.getLength() < MAXIMUM_TFTP_PACKET_SIZE)
				return;

			handleSocket(target, port, os);

		} finally {
			if (os != null)
				os.close();
		}
	}

	private void handleSocket(InetAddress target, int port, FileOutputStream os) throws IOException {
		try {
			DatagramPacket incoming;
			DatagramPacket outgoing;
			byte[] inbuf;

			while (true) {
				inbuf = new byte[516];
				incoming = new DatagramPacket(inbuf, inbuf.length);
				socket.receive(incoming);
				os.write(incoming.getData(), 4, incoming.getLength() - 4);

				ackNum++;
				byte[] b = setData(ackNum);
				outgoing = new DatagramPacket(b, b.length, target, port);
				socket.send(outgoing);

				if (incoming.getLength() < MAXIMUM_TFTP_PACKET_SIZE)
					break;
			}
		} catch (SocketTimeoutException e) {
			if (logger.isDebugEnabled())
				logger.debug("kraken tftp: socket timeout exception");
		}
	}

	private byte[] setData(short ackNum) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putShort((short) 4);
		bb.putShort(ackNum);

		bb.flip();
		byte[] b1 = new byte[4];
		bb.get(b1);
		return b1;
	}
}