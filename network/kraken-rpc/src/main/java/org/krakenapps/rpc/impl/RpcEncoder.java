/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.rpc.impl;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.rpc.RpcMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcEncoder extends OneToOneEncoder {
	private final Logger logger = LoggerFactory.getLogger(RpcEncoder.class.getName());

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		RpcMessage rpcMsg = (RpcMessage) msg;
		Object m = rpcMsg.marshal();

		int length = EncodingRule.lengthOf(m);
		ByteBuffer bb = ByteBuffer.allocate(length);
		EncodingRule.encode(bb, m);
		bb.flip();

		if (logger.isDebugEnabled())
			logger.debug("kraken-rpc: sending id: {}, method: {}, size: {}", new Object[] { rpcMsg.getHeader("id"),
					rpcMsg.getString("method"), bb.remaining() });

		return ChannelBuffers.wrappedBuffer(bb);
	}
}
