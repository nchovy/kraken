package org.krakenapps.msgbus;

public interface MessageListener {
	void onMessage(Session session, Message message);
}
