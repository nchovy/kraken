package org.krakenapps.termio;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TerminalInputStream extends InputStream {
	private BlockingQueue<Byte> queue;

	public TerminalInputStream() {
		queue = new ArrayBlockingQueue<Byte>(8096);
	}

	public void push(byte b) throws InterruptedException {
		queue.put(b);
	}

	@Override
	public int read() throws IOException {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

}
