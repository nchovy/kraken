package org.krakenapps.test;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteRequest {
	private SocketChannel channel;
	private ByteBuffer buffer;
	
	public WriteRequest(SocketChannel channel, ByteBuffer buffer) {
		this.channel = channel;
		this.buffer = buffer;
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


}
