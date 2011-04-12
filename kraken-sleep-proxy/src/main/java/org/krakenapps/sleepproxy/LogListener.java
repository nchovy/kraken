package org.krakenapps.sleepproxy;

import org.krakenapps.sleepproxy.model.Agent;
import org.krakenapps.sleepproxy.model.SleepLog.Status;

public interface LogListener {
	void onReceive(Agent agent, Status status);
}
