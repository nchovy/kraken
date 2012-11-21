package org.krakenapps.msgbus;

import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

@CollectionName("configs")
public class MsgbusConfig {
	
	// time unit is minute
	@FieldOption(name = "timeout", nullable = true)
	private Integer timeout;

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		return "timeout=" + timeout;
	}

}