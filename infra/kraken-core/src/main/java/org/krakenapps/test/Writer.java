package org.krakenapps.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Writer extends Thread {
	private BlockingQueue<WriteRequest> queue;
	
	public Writer() {
		this.queue = new LinkedBlockingQueue<WriteRequest>();
	}

	public void send(WriteRequest req) throws InterruptedException {
		queue.add(req);
	}

	public void run() {
		while (true) {
			try {
				WriteRequest req = queue.take();
				ByteBuffer buffer = req.getBuffer();
				buffer.position(0);
				
				req.getChannel().write(buffer);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
