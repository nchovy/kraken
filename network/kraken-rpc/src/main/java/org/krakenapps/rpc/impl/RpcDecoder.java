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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.krakenapps.codec.EncodingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcDecoder extends FrameDecoder {
	private final Logger logger = LoggerFactory.getLogger(RpcDecoder.class.getName());

	public static final int ARRAY_TYPE = 1;
	public static final int MAP_TYPE = 2;
	public static final int STRING_TYPE = 3;
	public static final int INT16_TYPE = 4;
	public static final int INT32_TYPE = 5;
	public static final int INT64_TYPE = 6;
	public static final int DATE_TYPE = 7;
	public static final int IPV4_TYPE = 8;
	public static final int IPV6_TYPE = 9;

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buf) throws Exception {
		if (logger.isDebugEnabled())
			logger.debug("kraken-rpc: current readable length {}", buf.readableBytes());

		buf.markReaderIndex();
		if (buf.readableBytes() < 2)
			return null;

		// read type byte
		buf.readByte();

		// read length bytes
		int lengthBytes = 0;
		byte b = 0;
		boolean eon = false;
		while (true) {
			if (buf.readableBytes() == 0)
				break;

			b = buf.readByte();
			lengthBytes++;

			if ((b & 0x80) != 0x80) {
				eon = true;
				break;
			}
		}

		if (!eon) {
			buf.resetReaderIndex();
			return null; // more length bytes needed
		}

		buf.resetReaderIndex();

		// read type byte
		buf.readByte();

		// byte buffer read does not modify readable index
		long length = EncodingRule.decodeRawNumber(buf.toByteBuffer());

		if (buf.readableBytes() >= lengthBytes + length) {
			buf.resetReaderIndex();
			ByteBuffer bb = ByteBuffer.allocate((int) length + lengthBytes + 1);
			buf.readBytes(bb);
			bb.flip();

			Object decoded = EncodingRule.decode(bb);
			if (logger.isDebugEnabled())
				logger.debug("kraken-rpc: decoded one message, remaining {}", buf.readableBytes());

			return decoded;
		}

		buf.resetReaderIndex();
		return null;
	}

}
