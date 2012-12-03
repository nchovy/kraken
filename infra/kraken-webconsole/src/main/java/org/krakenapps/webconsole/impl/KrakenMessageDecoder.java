/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webconsole.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.base64.Base64;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KrakenMessageDecoder {
	private KrakenMessageDecoder() {
	}

	public static Message decode(Session session, String text) {
		Logger logger = LoggerFactory.getLogger(KrakenMessageDecoder.class.getName());
		Charset utf8 = Charset.forName("utf-8");

		// remove potential control characters
		text = text.trim();
		if (text.length() == 0)
			return null;

		if (logger.isDebugEnabled())
			logger.debug("kraken webconsole: debug websocket frame length {}, json [{}]", text.length(), text);

		if (text.equals("ping"))
			return null;

		// decrypt if msg is encrypted
		if (session.has("enc_key")) {
			try {
				// jackson
				JsonNode container = new ObjectMapper().readTree(text);

				// 같은 작업 찾아야 할 부분
				// jackson
				Map<String, Object> header = new ObjectMapper().readValue(container.get(0),
						new TypeReference<Map<String, Object>>() {
						});

				Map<String, Object> body = new ObjectMapper().readValue(container.get(1),
						new TypeReference<Map<String, Object>>() {
						});

				if (header.containsKey("iv") && body.containsKey("data")) {
					String data = body.get("data").toString();

					byte[] iv = ByteUtil.asArray(Base64.decode(ChannelBuffers.wrappedBuffer(header.get("iv").toString()
							.getBytes())));
					byte[] buf = ByteUtil.asArray(Base64.decode(ChannelBuffers.wrappedBuffer(data.getBytes())));
					byte[] key = ByteUtil.asByteArray(UUID.fromString(session.getString("enc_key")));
					SecretKeySpec secret = new SecretKeySpec(key, "AES");

					Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
					cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
					byte[] plain = new byte[cipher.getOutputSize(buf.length)];
					int plainLength = cipher.update(buf, 0, buf.length, plain, 0);
					plainLength += cipher.doFinal(plain, plainLength);

					text = new String(plain, 0, plainLength, utf8);
					logger.trace("kraken webconsole: decrypted msg [{}]", text);
				}
			} catch (Exception e) {
				logger.error("kraken webconsole: cannot decode encrypted msg [" + text + "]", e);
			}
		}

		try {
			// jackson
			JsonNode container = new ObjectMapper().readTree(text);

			// 같은 작업 찾아야 할 부분
			// jackson
			Map<String, Object> header = new ObjectMapper().readValue(container.get(0), new TypeReference<Map<String, Object>>() {
			});

			Map<String, Object> body = new ObjectMapper().readValue(container.get(1), new TypeReference<Map<String, Object>>() {
			});

			Message msg = new Message();
			msg.setGuid((String) header.get("guid"));
			msg.setType(Message.Type.valueOf((String) header.get("type")));
			msg.setSource((String) header.get("source"));
			msg.setTarget((String) header.get("target"));
			msg.setMethod((String) header.get("method"));
			msg.setParameters(body);

			return msg;
		} catch (JsonProcessingException e) {
			logger.error("kraken webconsoke: invalid json Processing", e);
		} catch (IOException e) {
			logger.error("kraken webconsoke: invalid json read", e);
		}
		return null;
	}

}
