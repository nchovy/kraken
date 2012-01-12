package org.krakenapps.httpd.impl;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.replay.VoidEnum;

public class WebSocketFrameDecoderWithHost extends WebSocketFrameDecoder {

	private String host;

	public WebSocketFrameDecoderWithHost(String host, int maxFrameSize) {
		super(maxFrameSize);
		this.host = host;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, VoidEnum state)
			throws Exception {
		org.krakenapps.httpd.WebSocketFrame frame = new org.krakenapps.httpd.WebSocketFrame();
		WebSocketFrame f = (WebSocketFrame) super.decode(ctx, channel, buffer, state);
		frame.setHost(host);
		frame.setType(f.getType());
		frame.setText(f.getTextData());
		return frame;
	}

}
