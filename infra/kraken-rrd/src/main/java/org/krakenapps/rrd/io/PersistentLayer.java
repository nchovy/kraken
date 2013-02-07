/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.rrd.io;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class PersistentLayer implements DataInput, DataOutput, Closeable {
	public void open() {
		open(true);
	}

	public abstract void open(boolean read);

	public abstract int read(byte[] b) throws IOException;

	public abstract int read(byte[] b, int off, int len) throws IOException;

	public <T> T readEnum(Class<T> cls) throws IOException {
		if (!cls.isEnum())
			return null;

		String str = readUTF();
		if (str == null)
			return null;

		for (T t : cls.getEnumConstants()) {
			if (str.equals(t.toString()))
				return t;
		}

		return null;
	}

	public void writeEnum(Enum<?> e) throws IOException {
		writeUTF(e.toString());
	}
}
