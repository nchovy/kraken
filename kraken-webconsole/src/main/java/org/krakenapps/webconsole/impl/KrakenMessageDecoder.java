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

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.base64.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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

		// decrypt if msg is encrypted
		if (session.has("enc_key")) {
			try {
				JSONTokener tokenizer = new JSONTokener(new StringReader(text));
				JSONArray container = (JSONArray) tokenizer.nextValue();
				JSONObject header = container.getJSONObject(0);
				JSONObject body = container.getJSONObject(1);
				
				if (header.has("iv") && body.has("data")) {
					String data = body.getString("data");
					
					byte[] iv = ByteUtil.asArray(Base64.decode(ChannelBuffers.wrappedBuffer(header.getString("iv").getBytes())));
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
			if (text.equals("ping")) {
				return null;
			}
			JSONTokener tokenizer = new JSONTokener(new StringReader(text));
			JSONArray container = (JSONArray) tokenizer.nextValue();
			JSONObject header = container.getJSONObject(0);
			JSONObject body = container.getJSONObject(1);

			Message msg = new Message();

			msg.setGuid(header.getString("guid").trim());
			msg.setType(Message.Type.valueOf(header.getString("type").trim()));
			msg.setSource(header.getString("source"));
			msg.setTarget(header.getString("target"));
			msg.setMethod(header.getString("method").trim());
			msg.setParameters(parse(body));

			return msg;
		} catch (JSONException e) {
			logger.error("kraken webconsole: invalid json => " + text, e);
		}
		return null;
	}


	private static Map<String, Object> parse(JSONObject obj) {
		Map<String, Object> m = new HashMap<String, Object>();
		String[] names = JSONObject.getNames(obj);
		if (names == null)
			return m;

		for (String key : names) {
			try {
				Object value = obj.get(key);
				if (value == JSONObject.NULL)
					value = null;
				else if (value instanceof JSONArray)
					value = parse((JSONArray) value);
				else if (value instanceof JSONObject)
					value = parse((JSONObject) value);

				m.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return m;
	}

	private static List<Object> parse(JSONArray arr) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < arr.length(); i++) {
			try {
				Object o = arr.get(i);
				if (o == JSONObject.NULL)
					list.add(null);
				else if (o instanceof JSONArray)
					list.add(parse((JSONArray) o));
				else if (o instanceof JSONObject)
					list.add(parse((JSONObject) o));
				else
					list.add(o);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
