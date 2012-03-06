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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TftpServer implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(TftpServer.class.getName());

	private TftpRepository repos;
	private DatagramSocket listener;

	private volatile boolean isStop;
	private Thread runner;

	public TftpServer(String reposPath) throws FileNotFoundException, SocketException {
		repos = new TftpRepository(reposPath);
		listener = new DatagramSocket(69);
	}

	public void start() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	public void stop() {
		if (runner != null) {
			isStop = true;
			runner.interrupt();
			listener.close();
			runner = null;
		}
	}

	@Override
	public void run() {
		try {
			byte[] inbuf = new byte[600];
			DatagramPacket incoming = new DatagramPacket(inbuf, inbuf.length);

			while (!isStop) {
				listener.receive(incoming);
				handleIncoming(incoming);
			}
		} catch (IOException e) {
			// expected when closing socket
		}
	}

	private void handleIncoming(DatagramPacket incoming) {
		ByteBuffer bb = ByteBuffer.wrap(incoming.getData());
		short opCode = bb.getShort();

		/* abnormal case */
		if (opCode != 1 && opCode != 2)
			return;

		TftpMethod method = getMethod(opCode);
		String fileName = getFileName(bb);

		try {
			switch (method) {
			case GET:
				ServerGetProcessor processor = new ServerGetProcessor(incoming, repos.getPath()
						+ getSafeSource(fileName));
				processor.start();
				break;
			case PUT:
				ServerPutProcessor processor2 = new ServerPutProcessor(incoming);
				if (isSafeDestination(fileName))
					processor2.start(repos.getPath() + fileName);
				else
					processor2.block();
				break;
			}
		} catch (Exception e) {
			logger.error("kraken tftp: cannot handle incoming packet from " + incoming.getSocketAddress(), e);
		}
	}

	private TftpMethod getMethod(short opCode) {
		if (opCode == 1)
			return TftpMethod.GET;
		else
			return TftpMethod.PUT;
	}

	private String getFileName(ByteBuffer bb) {
		int length = getNullTerminatedLength(bb);
		byte[] name = new byte[length];
		bb.get(name);
		bb.get();

		return new String(name);
	}

	private int getNullTerminatedLength(ByteBuffer bb) {
		byte b;
		int length = 0;
		bb.mark();

		while (true) {
			try {
				b = bb.get();
				if (b == 0)
					break;
				length++;
			} catch (BufferUnderflowException e) {
				logger.error("kraken tftp: buffer underflow", e);
			}
		}
		bb.reset();

		return length;
	}

	private String getSafeSource(String path) {
		int pos = path.lastIndexOf("../");
		if (pos == -1)
			return path;
		else
			return path.substring(pos + 1);
	}

	private boolean isSafeDestination(String path) {
		return path.lastIndexOf("../") == -1;
	}
}