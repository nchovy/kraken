package org.krakenapps.test;

public interface DecoderOutput {
	void write(Object message);
	void setSession(Session session);
}
