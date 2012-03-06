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

public class FilterIdAttribute extends RadiusAttribute {

	private String name;

	public FilterIdAttribute(String name) {
		this.name = name;
	}

	public FilterIdAttribute(byte[] encoded, int offset, int length) {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not filter id attribute");

		this.name = decodeText(encoded, offset, length);
	}

	@Override
	public int getType() {
		return 11;
	}

	public String getName() {
		return name;
	}

	@Override
	public byte[] getBytes() {
		return encodeText(getType(), name);
	}

}
