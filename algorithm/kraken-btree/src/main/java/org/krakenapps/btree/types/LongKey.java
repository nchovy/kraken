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

public class LongKey extends RowKey {
	private long l;
	
	public LongKey(long l) {
		super(toBytes(l));
		this.l = l;
	}

	public LongKey(long l, byte[] b) {
		super(b);
		this.l = l;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (l ^ (l >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LongKey other = (LongKey) obj;
		if (l != other.l)
			return false;
		return true;
	}

	@Override
	public int compareTo(RowKey o) {
		if (!(o instanceof LongKey))
			throw new IllegalArgumentException("invalid key type: " + o.getClass().getName());

		LongKey other = (LongKey) o;
		return (int) (l - other.l);
	}

	public long getValue() {
		return l;
	}

	@Override
	public String toString() {
		return Long.toString(l);
	}

	private static byte[] toBytes(long l) {
		byte[] b = new byte[8];
		for (int i = 0; i < 8; i++) {
			b[i] = (byte) ((l >> (7 - i) * 8) & 0xff);
		}

		return b;
	}

}
