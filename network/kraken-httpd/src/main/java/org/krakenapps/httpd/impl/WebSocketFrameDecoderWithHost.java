/*
 * Copyright 2012 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.httpd.impl;

import java.net.InetSocketAddress;

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
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, VoidEnum state) throws Exception {
		org.krakenapps.httpd.WebSocketFrame frame = new org.krakenapps.httpd.WebSocketFrame();
		WebSocketFrame f = (WebSocketFrame) super.decode(ctx, channel, buffer, state);
		if (f == null)
			return null;

		frame.setRemote((InetSocketAddress) channel.getRemoteAddress());
		frame.setHost(host);
		frame.setType(f.getType());
		frame.setText(f.getTextData());
		return frame;
	}

}
