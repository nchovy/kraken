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
package org.krakenapps.btree.types;

import org.krakenapps.btree.RowKey;

public class IntegerKey extends RowKey {
	private int i;

	public IntegerKey(int i) {
		super(toBytes(i));
		this.i = i;
	}
	
	public IntegerKey(int i, byte[] b) {
		super(b);
		this.i = i;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntegerKey other = (IntegerKey) obj;
		if (i != other.i)
			return false;
		return true;
	}

	@Override
	public int compareTo(RowKey o) {
		if (!(o instanceof IntegerKey))
			throw new IllegalArgumentException("invalid key type: " + o.getClass().getName());

		IntegerKey other = (IntegerKey) o;
		return i - other.i;
	}
	
	public int getValue() {
		return i;
	}

	@Override
	public String toString() {
		return Integer.toString(i);
	}

	private static byte[] toBytes(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) ((i >> 24) & 0xff);
		b[1] = (byte) ((i >> 16) & 0xff);
		b[2] = (byte) ((i >> 8) & 0xff);
		b[3] = (byte) (i & 0xff);
		return b;
	}
}
