package org.krakenapps.test;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class Session {
	private SocketChannel channel;
	private ByteBuffer buffer;
	private Map<String, Object> attribute = new HashMap<String, Object>();
	private Handler handler;
	
	public Session(SocketChannel channel) {
		this.channel = channel;
		this.buffer = ByteBuffer.allocate(1024);
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public void setAttribute(String key, Object value) {
		this.attribute.put(key, value);
	}

	public Object getAttribute(String key) {
		return this.attribute.get(key); 
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public Handler getHandler() {
		return handler;
	}
}
