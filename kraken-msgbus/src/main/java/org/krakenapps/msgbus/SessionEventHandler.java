package org.krakenapps.msgbus;

public interface SessionEventHandler {
	void sessionOpened(Session session);
	
	void sessionClosed(Session session);
}
