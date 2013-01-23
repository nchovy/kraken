/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logstorage.index;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * file header
 * 
 * @since 0.9
 * @author xeraph
 */
public class InvertedIndexHeader {
	private Map<String, Object> headers;
	private int bodyOffset;

	public InvertedIndexHeader(Map<String, Object> headers) {
		this.headers = headers;
	}

	public int getVersion() {
		return (Integer) headers.get("version");
	}

	public String getType() {
		return (String) headers.get("type");
	}

	public Date getCreated() {
		return (Date) headers.get("created");
	}

	public Map<String, Object> getHeaders() {
		return new HashMap<String, Object>(headers);
	}

	public int getBodyOffset() {
		return bodyOffset;
	}

	public void setBodyOffset(int bodyOffset) {
		this.bodyOffset = bodyOffset;
	}
}
