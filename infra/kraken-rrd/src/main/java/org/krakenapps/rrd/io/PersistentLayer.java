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
