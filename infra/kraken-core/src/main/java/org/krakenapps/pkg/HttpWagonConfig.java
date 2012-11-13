package org.krakenapps.pkg;

import org.krakenapps.confdb.CollectionName;

@CollectionName("http_wagon_config")
public class HttpWagonConfig {
	private int connectTimeout = 10000;
	private int readTimeout = 10000;

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

}
