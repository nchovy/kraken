package org.krakenapps.msgbus;

import java.util.Collection;

public interface MessageHandler {
	void handleMessage(Request req, Response resp) throws Exception;

	String getClassName();

	Collection<String> getMethodNames();
}
