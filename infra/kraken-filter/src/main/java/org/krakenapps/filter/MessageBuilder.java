/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.filter;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.filter.impl.DefaultMessage;

/**
 * Builds a message of specific message specification. This class is for
 * convenience.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class MessageBuilder {
	private MessageSpec spec;
	private Map<String, Object> fields;
	private Map<String, Object> headers;

	/**
	 * Prepare a builder of specific message specification.
	 * 
	 * @param spec
	 *            the message specification
	 */
	public MessageBuilder(MessageSpec spec) {
		this.spec = spec;
		this.fields = new HashMap<String, Object>();
		this.headers = new HashMap<String, Object>();
	}

	/**
	 * Copy from the other message
	 * 
	 * @param message
	 *            the source message
	 * @return builder for method chaining
	 */
	public MessageBuilder setBase(Message message) {
		this.spec = message.getMessageSpec();

		for (String key : message.keySet()) {
			fields.put(key, message.get(key));
		}

		for (String key : message.headerKeySet()) {
			headers.put(key, message.getHeader(key));
		}

		return this;
	}

	/**
	 * Sets a property
	 * 
	 * @param key
	 *            the name of the property
	 * @param value
	 *            the value of the property
	 * @return builder for method chaining
	 */
	public MessageBuilder set(String key, Object value) {
		fields.put(key, value);
		return this;
	}

	/**
	 * Sets a header
	 * 
	 * @param key
	 *            the name of the header
	 * @param value
	 *            the value of the header
	 * @return builder for method chaining
	 */
	public MessageBuilder setHeader(String key, Object value) {
		headers.put(key, value);
		return this;
	}

	/**
	 * Creates a message instance.
	 * 
	 * @return the immutable message instance.
	 */
	public Message build() {
		Message message = new DefaultMessage(spec, fields, headers);
		fields = new HashMap<String, Object>();
		headers = new HashMap<String, Object>();
		return message;
	}
}
