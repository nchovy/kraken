package org.krakenapps.rpc.impl;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.krakenapps.codec.EncodingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelPipelineCoverage("one")
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

		if (buf.readableBytes() < 2)
			return null;

		buf.readByte();
		byte b = 0;
		boolean eon = false;
		while (true) {
			if (buf.readableBytes() == 0)
				break;

			b = buf.readByte();
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
		buf.readByte();
		long length = EncodingRule.decodeNumber(buf.toByteBuffer());

		if (buf.readableBytes() > length) {
			buf.resetReaderIndex();
			int numLength = EncodingRule.lengthOfNumber(int.class, length);
			ByteBuffer bb = ByteBuffer.allocate((int) length + numLength + 1);
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
