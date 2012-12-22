/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.dns;

public class DnsServiceStatus {
	private boolean running;
	private long receiveCount;
	private long dropCount;

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public long getQueryCount() {
		return receiveCount;
	}

	public void setReceiveCount(long receiveCount) {
		this.receiveCount = receiveCount;
	}

	public long getDropCount() {
		return dropCount;
	}

	public void setDropCount(long dropCount) {
		this.dropCount = dropCount;
	}

	@Override
	public String toString() {
		return "running=" + running + ", query count=" + receiveCount + ", drop count=" + dropCount;
	}
}
