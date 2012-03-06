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
package org.krakenapps.filter.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.krakenapps.filter.Message;
import org.krakenapps.filter.MessageSpec;

/**
 * This class provides default implementation for the {@link Message} interface.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class DefaultMessage implements Message {
	private MessageSpec spec;
	private Map<String, Object> fields;
	private Map<String, Object> headers;

	public DefaultMessage(MessageSpec spec, Map<String, Object> fields, Map<String, Object> headers) {
		this.spec = spec;
		this.fields = new HashMap<String, Object>();
		this.headers = new HashMap<String, Object>();

		for (String key : fields.keySet()) {
			this.fields.put(key, fields.get(key));
		}

		for (String key : headers.keySet()) {
			this.headers.put(key, headers.get(key));
		}
	}

	public DefaultMessage(Message message) {
		this.spec = message.getMessageSpec();
		this.fields = new HashMap<String, Object>();
		this.headers = new HashMap<String, Object>();

		for (String key : message.keySet()) {
			this.fields.put(key, message.get(key));
		}

		for (String key : message.headerKeySet()) {
			this.headers.put(key, message.getHeader(key));
		}
	}

	@Override
	public Set<String> headerKeySet() {
		return headers.keySet();
	}

	@Override
	public Set<String> keySet() {
		return fields.keySet();
	}

	@Override
	public Object get(String key) {
		return fields.get(key);
	}

	@Override
	public Object getHeader(String key) {
		return headers.get(key);
	}

	@Override
	public MessageSpec getMessageSpec() {
		return spec;
	}

	@Override
	public boolean containsHeader(String key) {
		return headers.containsKey(key);
	}

	@Override
	public boolean containsKey(String key) {
		return fields.containsKey(key);
	}

	@Override
	public Map<String, Object> unmodifiableHeaderMap() {
		return Collections.unmodifiableMap(headers);
	}

	@Override
	public Map<String, Object> unmodifiableFieldMap() {
		return Collections.unmodifiableMap(fields);
	}

}
