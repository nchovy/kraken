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
package org.krakenapps.radius.protocol;

public class ReplyMessageAttribute extends RadiusAttribute {
	private String message;

	public ReplyMessageAttribute(String message) {
		this.message = message;
	}

	public ReplyMessageAttribute(byte[] encoded, int offset, int length) {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not reply message attribute");

		this.message = decodeText(encoded, offset, length);
	}

	@Override
	public int getType() {
		return 18;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public byte[] getBytes() {
		return encodeText(getType(), message);
	}

	@Override
	public String toString() {
		return "Reply-Message: " + message;
	}
}
