/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.pcap.decoder.tcp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mindori
 */
public class WaitQueue {
	private final List<TcpPacket> queue;

	public WaitQueue() {
		queue = new ArrayList<TcpPacket>();
	}

	public WaitQueue(int capacity) {
		queue = new ArrayList<TcpPacket>(capacity);
	}

	public void enqueue(TcpPacket element) {
		queue.add(element);
	}

	public TcpPacket dequeue(int index) {
		return queue.get(index);
	}

	public void remove(int index) {
		queue.remove(index);
	}

	public int size() {
		return queue.size();
	}
}