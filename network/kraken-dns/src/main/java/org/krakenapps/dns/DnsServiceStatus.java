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
