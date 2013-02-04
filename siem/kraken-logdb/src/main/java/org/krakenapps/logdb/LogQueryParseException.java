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
package org.krakenapps.logdb;

public class LogQueryParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String type;
	private Integer offset;
	private String note;

	public LogQueryParseException(String type, int offset) {
		this(type, offset, null);
	}
	
	public LogQueryParseException(String type, int offset, String note) {
		this.type = type;
		this.offset = offset;
		this.note = note;
	}

	public String getType() {
		return type;
	}

	public Integer getOffset() {
		return offset;
	}

	@Override
	public String getMessage() {
		return "type=" + type + ", offset=" + offset + ", note=" + note;
	}

}
