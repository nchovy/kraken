package org.krakenapps.test;

import java.io.IOException;

public interface IHandler {
	public void sessionOpened(Session session, Writer writer, Encoder encoder);
	
	public void sessionClosed(Session session);
	
	public void messageReceived(Session session, Object message) throws IOException;
	
	public void exceptionCaught(Session session);
}
