package org.krakenapps.honey.sshd;

public class HoneyPath {
	private String path;

	public HoneyPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return path;
	}
}
