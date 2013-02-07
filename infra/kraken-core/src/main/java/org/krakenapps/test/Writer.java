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
