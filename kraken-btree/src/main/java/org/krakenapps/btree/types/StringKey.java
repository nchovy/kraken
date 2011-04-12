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

import java.nio.charset.Charset;

import org.krakenapps.btree.RowKey;

public class StringKey extends RowKey {
	private static final Charset UTF8 = Charset.forName("utf-8");
	private String s;

	public StringKey(String s) {
		super(s.getBytes(UTF8));
		this.s = s;
	}

	public StringKey(byte[] b) {
		super(b);
		this.s = new String(b, UTF8);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((s == null) ? 0 : s.hashCode());
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
		StringKey other = (StringKey) obj;
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}

	@Override
	public int compareTo(RowKey o) {
		if (!(o instanceof StringKey))
			throw new IllegalArgumentException("invalid key type: " + o.getClass().getName());
		
		StringKey other = (StringKey) o;
		return s.compareTo(other.s);
	}

}
