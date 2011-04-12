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

public class CompositeKey extends RowKey {
	private Object[] keys;

	public CompositeKey(CompositeKeyFactory factory, byte[] b) {
		super(b);
		this.keys = KeyEncoder.decode(factory.getKeyTypes(), b);
	}

	public CompositeKey(Object... keys) {
		super(KeyEncoder.encode(keys));
		this.keys = keys;
	}

	public Object[] getKeys() {
		return keys;
	}

	@Override
	public int compareTo(RowKey o) {
		if (!(o instanceof CompositeKey))
			throw new IllegalArgumentException("invalid composite key type: " + o.getClass().getName());

		CompositeKey other = (CompositeKey) o;

		// compare all keys
		for (int i = 0; i < keys.length; i++) {
			Object item = keys[i];
			if (item instanceof Integer) {
				int ret = (Integer) item - (Integer) other.keys[i];
				if (ret != 0)
					return ret;
			} else if (item instanceof Long) {
				int ret = (int) ((Long) item - (Long) other.keys[i]);
				if (ret != 0)
					return ret;
			} else if (item instanceof String) {
				int ret = ((String) item).compareTo((String) other.keys[i]);
				if (ret != 0)
					return ret;
			}
		}

		return 0;
	}

}
