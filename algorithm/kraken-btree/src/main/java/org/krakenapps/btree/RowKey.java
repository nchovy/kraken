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
package org.krakenapps.btree;

public class RowKey extends RowEntry implements Comparable<RowKey> {

	public RowKey(byte[] bytes) {
		super(bytes);
	}

	@Override
	public int compareTo(RowKey o) {
		if (o == null)
			throw new IllegalArgumentException("passed null key");

		if (!(o instanceof RowKey))
			throw new IllegalArgumentException("invalid key type");

		int length = Math.min(bytes.length, o.bytes.length);
		for (int i = 0; i < length; i++) {
			if (bytes[i] < o.bytes[i])
				return -1;
			else if (bytes[i] > o.bytes[i])
				return 1;
		}

		if (bytes.length != o.bytes.length)
			return bytes.length - o.bytes.length;

		return 0;
	}
}
